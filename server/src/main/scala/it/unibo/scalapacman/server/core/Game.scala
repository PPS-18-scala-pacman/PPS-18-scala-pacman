package it.unibo.scalapacman.server.core

import akka.actor.typed.{ActorRef, Behavior, ChildFailed, MailboxSelector, Terminated}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.model.ws.Message
import it.unibo.scalapacman.lib.model.GhostType.{BLINKY, CLYDE, GhostType, INKY, PINKY}
import it.unibo.scalapacman.lib.model.PacmanType.PacmanType
import it.unibo.scalapacman.server.core.Engine.{EngineCommand, Start}
import it.unibo.scalapacman.server.core.Game.{CloseCommand, GameCommand, Model, NotifyPlayerReady, PlayerData, PlayerLeftGame, RegisterPlayer, Setup}
import it.unibo.scalapacman.server.config.Settings
import it.unibo.scalapacman.server.core.PlayerAct.{PlayerRegistration, RegistrationRejected}
import it.unibo.scalapacman.server.model.GameEntity

import scala.util.Random

/**
 * Attore che si occupa di predisporre le risorse necessarie al regolare svolgersi di una sessione di gioco,
 * si occupa di generare un attore Engine, Player e GhostAct, per ogni fantasma esistente.
 * Inoltre svolge un ruolo di primaria importanza nel garantire un elevato livello di fault-tolerance, svolge
 * un'attività di monitoraggio dei propri attori figli grazie al quale è in grado rilevare eventuali anomalie.
 */
object Game {

  // Messaggi gestiti dall'attore
  sealed trait GameCommand
  case class CloseCommand() extends GameCommand
  case class RegisterPlayer(replyTo: ActorRef[PlayerRegistration], source: ActorRef[Message], nickname: String) extends GameCommand
  case class NotifyPlayerReady(nickname: String) extends GameCommand
  case class PlayerLeftGame(nickname: String) extends GameCommand

  private case class PlayerData(nickname: String, isReady: Boolean, hasLeft: Boolean = false)
  private case class GhostData(nickname: String, ghostType: GhostType)

  private case class Setup( id: String,
                            context: ActorContext[GameCommand],
                            engine: ActorRef[EngineCommand],
                            components: Map[String, PacmanType])

  private case class Model(players: Map[ActorRef[PlayerAct.PlayerCommand], PlayerData],
                           ghosts: Map[ActorRef[Engine.UpdateCommand], GhostData],
                           gameStarted: Boolean)

  def apply(id: String, components: Map[String, PacmanType], visible: Boolean = true): Behavior[GameCommand] = {
    require(components.nonEmpty || components.size <= Settings.maxPlayersNumber, "Numero di giocatori errato")
    Behaviors.setup { context =>

      if(visible) {
        val gameServiceKey: ServiceKey[GameCommand] = ServiceKey[GameCommand](id)
        context.system.receptionist ! Receptionist.Register(gameServiceKey, context.self)
      }

      // inizializzazione attori partecipanti
      val defaultGhosts = List(BLINKY, INKY, PINKY, CLYDE)
      implicit val generator: Random = new Random(System.currentTimeMillis())
      val entityList: List[GameEntity] = defaultGhosts.map(gt => GameEntity(generateGhostId(gt, components.keys.toList), gt)) :::
        components.map(cp => GameEntity(cp._1, cp._2)).toList

      val engine = context.spawn(Engine(id, entityList, Settings.levelDifficulty), "EngineActor")

      val props = MailboxSelector.fromConfig("ghost-mailbox")
      val ghosts = entityList.filter(_.charType.isInstanceOf[GhostType]).map(gt =>
        context.spawn( GhostAct(id, engine, gt.nickname), gt.charType + "Actor", props) -> GhostData(gt.nickname, gt.charType.asInstanceOf[GhostType])
      ).toMap

      (Set(engine) ++ ghosts.keySet).foreach(context.watch(_))

      new Game(Setup(id, context, engine, components)).start(Model(Map(), ghosts, gameStarted = false))
    }
  }

  private def generateGhostId(ghostType: GhostType, usedId: List[String])(implicit generator: Random): String = {
    lazy val genRandom: Stream[String] = { generator.nextInt().toString #:: genRandom }
    genRandom.iterator.map(ghostType.toString + _).dropWhile(usedId.contains).next()
  }
}

private class Game(setup: Setup) {

  /**
   * Behavior iniziale di attesa giocatore pacman
   */
  private def idleRoutine(model: Model): Behaviors.Receive[Game.GameCommand] =
    Behaviors.receiveMessage {
      case CloseCommand() => close()
      case RegisterPlayer(replyTo, source, nickname) => handlePlayerRegistration(replyTo, source, nickname, model)
      case NotifyPlayerReady(nickname) => handlePlayerChange(model, nickname, x => x.copy(isReady = true))
      case PlayerLeftGame(nickname) =>
        if (model.players.exists(_._2.nickname == nickname)) setup.engine ! Engine.DisablePlayer(nickname)
        handlePlayerChange(model, nickname, x => x.copy(hasLeft = true))
    }

  /**
   * Behavior principale
   */
  private def runRoutine(model: Model): Behaviors.Receive[Game.GameCommand] =
    Behaviors.receiveMessage {
      case CloseCommand() => close()
      case RegisterPlayer(replyTo, _, _) =>
        replyTo ! RegistrationRejected("Gioco in corso")
        prepareBehavior(runRoutine, model)
      case PlayerLeftGame(nickname) =>
        val player = model.players.find(_._2.nickname == nickname)
        if(player.isEmpty) {
          prepareBehavior(runRoutine, model)
        } else {
          val updatedPlayers = model.players + (player.get._1 -> player.get._2.copy(hasLeft = true))
          if(updatedPlayers.exists(!_._2.hasLeft)) {
            setup.engine ! Engine.DisablePlayer(nickname)
            prepareBehavior(runRoutine, model.copy(players = updatedPlayers))
          } else {
            close()
          }
        }
      case _ =>
        setup.context.log.warn("Ricevuto messaggio non gestito")
        prepareBehavior(runRoutine, model)
    }

  private def close(): Behavior[GameCommand] = {
    setup.context.log.info("Chiusura partita: " + setup.id)
    Behaviors.stopped
  }

  private def start(model: Model): Behavior[GameCommand] = {
    prepareBehavior(idleRoutine, model)
  }

  private def handlePlayerRegistration(replyTo: ActorRef[PlayerRegistration],
                                       source: ActorRef[Message],
                                       nickname: String,
                                       model: Model) = {
    setup.context.log.info("RegisterPlayer ricevuto")

    if(!setup.components.contains(nickname) || model.players.exists(_._2.nickname == nickname)) {
      setup.context.log.error(s"Nickname: $nickname, non valido")
      replyTo ! RegistrationRejected("Giocatore non valido")
      prepareBehavior(idleRoutine, model)
    } else {

      val player = setup.context.spawn(PlayerAct(setup.id, setup.engine, setup.context.self), s"${nickname}Actor")
      setup.context.watch(player)
      player ! PlayerAct.RegisterUser(replyTo, source, nickname)
      val updatedPlayers = model.players + (player -> PlayerData(nickname, isReady = false))
      prepareBehavior(idleRoutine, model.copy(players = updatedPlayers))
    }
  }

  private def handlePlayerChange(model: Model, nickname: String, editFunc: Game.PlayerData => Game.PlayerData) = {
    val player = model.players.find(_._2.nickname == nickname)
    if(player.isEmpty) {
      prepareBehavior(idleRoutine, model)
    } else {
      val updatedPlayers = model.players + (player.get._1 -> editFunc(player.get._2))
      if(setup.components.size == updatedPlayers.count(player => player._2.isReady || player._2.hasLeft)) {
        if(updatedPlayers.exists(!_._2.hasLeft)) {
          if(!model.gameStarted) setup.engine ! Start()
          prepareBehavior(runRoutine, model.copy(players = updatedPlayers, gameStarted = true))
        } else {
          close()
        }
      } else {
        prepareBehavior(idleRoutine, model.copy(players = updatedPlayers))
      }
    }
  }

  /**
   * funzione di utility per aggiungere la configurazione di gestione segnali di errore degli attori figli al
   * Behavior futuro
   *
   * @param recBe receiveBehavior
   * @param model modello del Game
   * @return      Behavior futuro
   */
  private def prepareBehavior(recBe: Model => Behaviors.Receive[Game.GameCommand],
                              model: Model): Behavior[Game.GameCommand] =
    recBe(model).receiveSignal {
      case (context, ChildFailed(act@setup.engine, _)) =>
        context.log.error(s"$act engine crashed")
        Behaviors.stopped
      case (context, ChildFailed(act, _)) if act.isInstanceOf[ActorRef[PlayerAct.PlayerCommand]] =>
        context.log.error(s"$act player crashed")
        val playerAct = act.asInstanceOf[ActorRef[PlayerAct.PlayerCommand]]
        if(!model.players(playerAct).hasLeft) {
          setup.engine ! Engine.Pause()
          val updatedModel = model.copy(players = model.players - playerAct)
          prepareBehavior(idleRoutine, updatedModel)
        } else {
          prepareBehavior(recBe, model)
        }
      case (context, ChildFailed(act, _)) if act.isInstanceOf[ActorRef[Engine.UpdateCommand]] =>
        context.log.error(s"$act ghost stopped")
        val ghostAct = act.asInstanceOf[ActorRef[Engine.UpdateCommand]]
        val ghostData = model.ghosts.get(ghostAct)
        if(ghostData.isDefined) {
          setup.engine ! Engine.Pause()

          val props = MailboxSelector.fromConfig("ghost-mailbox")
          val ghost = context.spawn(GhostAct(setup.id, setup.engine, ghostData.get.nickname), ghostAct.path.name, props)
          context.watch(ghost)
          val updatedGhosts = (model.ghosts - ghostAct) + (ghost -> ghostData.get)
          prepareBehavior(recBe, model.copy(ghosts = updatedGhosts))
        } else {
          context.log.error(s"$ghostAct non è un attore giocante")
          prepareBehavior(recBe, model)
        }
      case (context, Terminated(ref)) =>
        context.log.info(s"Attore terminato: $ref")
        prepareBehavior(recBe, model)
      case (context, sign) =>
        context.log.debug(s"Ricevuto signal non gestita: $sign")
        prepareBehavior(recBe, model)
    }
}
