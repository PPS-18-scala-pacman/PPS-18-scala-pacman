package it.unibo.scalapacman.server.core

import akka.actor.typed.{ActorRef, Behavior, ChildFailed, MailboxSelector, Terminated}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.model.ws.Message
import it.unibo.scalapacman.common.GameCharacter
import it.unibo.scalapacman.lib.model.GhostType
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.server.core.Engine.EngineCommand
import it.unibo.scalapacman.server.core.Game.{CloseCommand, GameCommand, Model, RegisterPlayer, Setup}
import it.unibo.scalapacman.server.core.Player.{PlayerCommand, PlayerRegistration, RegistrationRejected}
import it.unibo.scalapacman.server.config.Settings

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
  case class RegisterPlayer(replyTo: ActorRef[PlayerRegistration], source: ActorRef[Message]) extends GameCommand

  private case class Setup( id: String,
                            context: ActorContext[GameCommand],
                            engine: ActorRef[EngineCommand])

  private case class Model( player: ActorRef[PlayerCommand],
                            ghosts: Map[ActorRef[Engine.UpdateCommand],GhostType])

  def apply(id: String, visible: Boolean = true): Behavior[GameCommand] =
    Behaviors.setup { context =>

      if(visible) {
        val gameServiceKey: ServiceKey[GameCommand] = ServiceKey[GameCommand](id)
        context.system.receptionist ! Receptionist.Register(gameServiceKey, context.self)
      }

      // inizializzazione attori partecipanti
      val engine = context.spawn(Engine(id, Settings.levelDifficulty), "EngineActor")
      val player = context.spawn(Player(id, engine), "PlayerActor")

      val props  = MailboxSelector.fromConfig("server-app.ghost-mailbox")
      val ghosts = GhostType.values.map( gt =>
        context.spawn(GhostAct(id, engine, gt), s"${gt}Actor", props) -> gt
      ).toMap

      (Set(engine, player) ++ ghosts.keySet).foreach(context.watch(_))

      new Game(Setup(id, context, engine)).start(Model(player, ghosts))
    }

}

private class Game(setup: Setup) {

  /**
   * Behavior iniziale di attesa giocatore pacman
   */
  private def initRoutine(model: Model): Behaviors.Receive[Game.GameCommand] =
    Behaviors.receiveMessage {
      case CloseCommand() => close()
      case RegisterPlayer(replyTo, source) =>
        setup.context.log.info("RegisterPlayer ricevuto")
        model.player ! Player.RegisterUser(replyTo, source)
        prepareBehavior(coreRoutine, model)
    }

  /**
   * Behavior principale
   */
  private def coreRoutine(model: Model): Behaviors.Receive[Game.GameCommand] =
    Behaviors.receiveMessage {
      case CloseCommand() => close()
      case RegisterPlayer(replyTo, _) =>
        replyTo ! RegistrationRejected("Gioco in corso")
        prepareBehavior(coreRoutine, model)
    }

  private def close(): Behavior[GameCommand] = {
    setup.context.log.info("CloseCommand ricevuto")
    Behaviors.stopped
  }

  private def start(model: Model): Behavior[GameCommand] = {
    prepareBehavior(initRoutine, model)
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
        context.log.error(s"$act crashed")
        Behaviors.stopped
      case (context, ChildFailed(act@model.player, _)) =>
        context.log.error(s"$act stopped")
        setup.engine ! Engine.ActorRecovery(GameCharacter.PACMAN)
        val player = context.spawn(Player(setup.id, setup.engine), "PlayerActor")
        prepareBehavior(initRoutine, model.copy(player = player))
      case (context, ChildFailed(act, _)) if act.isInstanceOf[ActorRef[Engine.UpdateCommand]] =>
        context.log.error(s"$act stopped")
        val ghostAct = act.asInstanceOf[ActorRef[Engine.UpdateCommand]]
        val ghostType = model.ghosts.get(ghostAct)
        if(ghostType.isDefined) {
          setup.engine ! Engine.ActorRecovery(ghostType.get)

          val props = MailboxSelector.fromConfig("server-app.ghost-mailbox")
          val ghost = context.spawn(GhostAct(setup.id, setup.engine, ghostType.get), s"${ghostType.get}Actor", props)
          val updatedGhosts = (model.ghosts - ghostAct) + (ghost -> ghostType.get)
          prepareBehavior(recBe, model.copy(ghosts = updatedGhosts))
        } else {
          context.log.error(s"$ghostAct non è un attore noto")
          prepareBehavior(recBe, model)
        }
      case (context, Terminated(ref)) =>
        context.log.info(s"Attore terminato: $ref")
        Behaviors.same
    }
}
