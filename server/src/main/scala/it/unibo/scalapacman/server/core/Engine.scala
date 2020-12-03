package it.unibo.scalapacman.server.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.scalapacman.common.DotDTO.rawToDotDTO
import it.unibo.scalapacman.common.FruitDTO.rawToFruitDTO
import it.unibo.scalapacman.common.{DotDTO, FruitDTO, GameEntityDTO, UpdateModelDTO}
import it.unibo.scalapacman.lib.engine.{GameMovement, GameTick}
import it.unibo.scalapacman.lib.engine.GameHelpers.MapHelper
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.lib.model.PacmanType.PacmanType
import it.unibo.scalapacman.lib.model.{Character, GameObject, Level, LevelState, Map}
import it.unibo.scalapacman.server.config.Settings
import it.unibo.scalapacman.server.core.Engine.{ChangeDirectionCur, ChangeDirectionReq, DelayedResume, DisablePlayer,
  EngineCommand, Model, Pause, RegisterWatcher, Resume, Setup, Start, UnRegisterWatcher, UpdateCommand, UpdateMsg, WakeUp}
import it.unibo.scalapacman.server.model.GameParticipant.gameParticipantToGameEntity
import it.unibo.scalapacman.server.model.MoveDirection.MoveDirection
import it.unibo.scalapacman.server.model.{GameData, GameEntity, GameParameter, GameParticipant}

import scala.concurrent.duration.FiniteDuration

/**
 * Attore che si occupa, per ogni partita, di effettuare l’elaborazione dello stato di avanzamento del gioco.
 * Dopo una fase preliminare di registrazione degli attori Player e GhostAct effettua periodicamente le dovute
 * elaborazioni e in seguito provvede a comunicare lo stato aggiornato agli attori coinvolti.
 * Oltre a questo è provvisto di una logica di recupero in caso di guasto ed oltre ad essere in grado di
 * ricevere le richieste, da parte degli attori partecipanti, di modifica della propria direzione, è anche
 * dotato della capacità di sospendere temporaneamente la sessione di gioco e di riprenderla successivamente.
 */
object Engine {

  // Messaggi gestiti dall'attore
  sealed trait EngineCommand

  case class WakeUp() extends EngineCommand
  case class Pause() extends EngineCommand
  case class Start() extends EngineCommand
  case class Resume() extends EngineCommand
  private case class DelayedResume() extends EngineCommand
  case class ChangeDirectionReq(nickname: String, direction: MoveDirection) extends EngineCommand
  case class ChangeDirectionCur(nickname: String) extends EngineCommand
  case class RegisterWatcher(actor: ActorRef[UpdateCommand]) extends EngineCommand
  case class UnRegisterWatcher(actor: ActorRef[UpdateCommand]) extends EngineCommand
  case class DisablePlayer(nickname: String) extends EngineCommand

  // Messaggi inviati agli osservatori
  sealed trait UpdateCommand
  case class UpdateMsg(model: UpdateModelDTO) extends UpdateCommand

  private case class Setup(gameId: String, context: ActorContext[EngineCommand], info: GameParameter)

  private case class Model(watchers: Set[ActorRef[UpdateCommand]], data: GameData)

  def apply(gameId: String, entities: List[GameEntity], level: Int): Behavior[EngineCommand] =
    Behaviors.setup { context =>
      new Engine(Setup(gameId, context, GameParameter(entities, level))).initRoutine(Set(), Set())
    }
}

private class Engine(setup: Setup) {

  /**
   * Behavior iniziale, gestisce la prima registrazione da parte degli attori partecipanti alla partita
   */
  private def initRoutine(watchers: Set[ActorRef[UpdateCommand]], disabledPlayers: Set[String]): Behavior[EngineCommand] =
    Behaviors.receiveMessage {

      case RegisterWatcher(actor) => initRoutine(watchers = watchers + actor, disabledPlayers)
      case UnRegisterWatcher(actor) => initRoutine(watchers = watchers - actor, disabledPlayers)
      case Start() =>
        setup.context.log.info("Start id: " + setup.gameId)
        delayRoutine(initEngineModel(watchers, disabledPlayers), Settings.gameDelay)
      case DisablePlayer(nickname) =>
        setup.context.log.info("Remove player " + nickname + ", id: " + setup.gameId)
        initRoutine(watchers, disabledPlayers = disabledPlayers + nickname)
      case _ => unhandledMsg()
    }

  private def delayRoutine(model: Model, delay: FiniteDuration): Behavior[EngineCommand] = {
    updateWatchers(model)
    setup.context.scheduleOnce(delay, setup.context.self, DelayedResume())

    Behaviors.withStash(Settings.stashSize) { buffer =>
      Behaviors.receiveMessage {
        case DelayedResume() =>
          setup.context.log.info("Attesa finita, gioco in avvio: " + setup.gameId)
          buffer.unstashAll(mainRoutine(model))
        case other: EngineCommand =>
          buffer.stash(other)
          Behaviors.same
      }
    }
  }

  /**
   * Behavior utilizzato nel caso in cui il gioco venga messo in pausa, esegue l'invio di aggiornamenti
   * senza procedere all'aggiornamento dello stato della partita
   */
  private def pauseRoutine(model: Model): Behavior[EngineCommand] = {
    Behaviors.withTimers { timers =>
      timers.startTimerWithFixedDelay(WakeUp(), WakeUp(), setup.info.pauseRefreshRate)

      Behaviors.receiveMessage {
        case RegisterWatcher(act) =>
          setup.context.watchWith(act, UnRegisterWatcher(act))
          pauseRoutine(addWatcher(model, act))
        case UnRegisterWatcher(act) => pauseRoutine(removeWatcher(model, act))
        case WakeUp() =>
          updateWatchers(model)
          Behaviors.same
        case Resume() =>
          setup.context.log.info("Resume id: " + setup.gameId)
          timers.cancel(WakeUp())
          mainRoutine(model)
        case Pause() => Behaviors.same
        case ChangeDirectionCur(nickname) => clearDesiredDir(model, nickname, pauseRoutine)
        case ChangeDirectionReq(nickname, dir) => changeDesiredDir(model, nickname, dir, pauseRoutine)
        case DisablePlayer(nickname) => pauseRoutine(removePlayer(model, nickname))
        case _ => unhandledMsg()
      }
    }
  }

  /**
   * Behavior utilizzato durante il corso della partita, effettua il ricalcolo periodico dello stato della
   * partita e si occupa di aggiornare gli osservatori, gestisce le richieste di pausa e cambio direzione
   */
  private def mainRoutine(model: Model): Behavior[EngineCommand] =
    Behaviors.withTimers { timers =>
      timers.startTimerWithFixedDelay(WakeUp(), WakeUp(), setup.info.gameRefreshRate)

      Behaviors.receiveMessage {
        case RegisterWatcher(act) => mainRoutine(addWatcher(model, act))
        case UnRegisterWatcher(act) => mainRoutine(removeWatcher(model, act))
        case WakeUp() => updateGame(model)
        case Pause() =>
          setup.context.log.info("Pause id: " + setup.gameId)
          timers.cancel(WakeUp())
          pauseRoutine(model)
        case ChangeDirectionCur(nickname) => clearDesiredDir(model, nickname, mainRoutine)
        case ChangeDirectionReq(nickname, dir) => changeDesiredDir(model, nickname, dir, mainRoutine)
        case DisablePlayer(nickname) => mainRoutine(removePlayer(model, nickname))
        case _ => unhandledMsg()
      }
    }

  /**
   * Crea il dto contentente lo stato della partita a partire dal modello di gioco
   *
   * @param model  modello contenente lo stato corrente del gioco
   * @return       dto per aggiornamento osservatori
   */
  private def elaborateUpdateModel(model: GameData): UpdateModelDTO = {

    val gameEntities: Set[GameEntityDTO] = model.participants.toSet.map(gameParticipantToGameEntity)
    val dots: Set[DotDTO]                = model.map.dots.map(rawToDotDTO).toSet
    val fruit: Option[FruitDTO]          = model.map.fruit.map(rawToFruitDTO)

    UpdateModelDTO(gameEntities, model.state, dots, fruit)
  }

  private def initEngineModel(watcher: Set[ActorRef[UpdateCommand]], disabledPlayers: Set[String]): Model = {
    val classicFactory = Level.Classic(setup.info.level)
    val participants: List[GameParticipant] = setup.info.players
      .filter(p => !disabledPlayers.contains(p.nickname))
      .map(char => char.charType match {
        case typ: GhostType   => GameParticipant(char.nickname, classicFactory.ghost(typ))
        case typ: PacmanType  => GameParticipant(char.nickname, classicFactory.pacman(typ))
      })
    Model(watcher, GameData(participants, classicFactory.map, classicFactory.gameState, classicFactory.gameEvents))
  }

  private def updateWatchers(model: Model): Unit =
    model.watchers.foreach( _ ! UpdateMsg(elaborateUpdateModel(model.data)) )

  private def addWatcher(model: Model, watcher: ActorRef[UpdateCommand]): Model = {
    setup.context.watchWith(watcher, UnRegisterWatcher(watcher))
    model.copy(watchers = model.watchers + watcher)
  }

  private def removePlayer(model: Model, nickname: String): Model = {
    setup.context.log.info("Remove player " + nickname + ", id: " + setup.gameId)
    val updatedParticipants = model.data.participants.filter(_.nickname != nickname)
    model.copy(data = model.data.copy(participants = updatedParticipants))
  }

  private def removeWatcher(model: Model, watcher: ActorRef[UpdateCommand]): Model =
    model.copy(watchers = model.watchers - watcher)

  /**
   * Calcolo del nuovo stato di gioco a partire dal precedente
   */
  private def updateGame(model: Model) : Behavior[EngineCommand] = {
    setup.context.log.debug("updateGame id: " + setup.gameId)
    implicit val map: Map = model.data.map
    implicit var players: List[Character] = model.data.participants.map(updateChar).map(_.character)
    implicit val collisions: List[(Character, GameObject)] = GameTick.collisions(players)

    var newMap = GameTick.calculateMap(map)
    var state = GameTick.calculateGameState(model.data.state)
    players = GameTick.calculateDeaths(players, state)
    players = GameTick.calculateSpeeds(players, setup.info.level, state)
    state = GameTick.calculateLevelState(state, players, map)

    var gameEvents = model.data.gameEvents
    gameEvents = GameTick.consumeTimeOfGameEvents(gameEvents, setup.info.gameRefreshRate)
    gameEvents = GameTick.updateEvents(gameEvents, state, collisions)
    players = GameTick.handleEvents(gameEvents, players)
    newMap = GameTick.handleEvents(gameEvents, newMap)
    state = GameTick.handleEvents(gameEvents, state)
    gameEvents = GameTick.removeTimedOutGameEvents(gameEvents)

    val updatePlayers = model.data.participants.flatMap(par =>
      players.find(_.characterType == par.character.characterType).map(c => par.copy(character = c))
    )

    val gameData: GameData = GameData(updatePlayers, newMap, state, gameEvents)
    val updateModel = model.copy(data = gameData)
    updateWatchers(updateModel)

    if(state.levelState == LevelState.ONGOING) {
      mainRoutine(updateModel)
    } else {
      setup.context.log.info("Partita terminata spegnimento")
      Behaviors.stopped
    }
  }

  private def updateChar(participant: GameParticipant)(implicit map: Map): GameParticipant = {
    val newChar = GameMovement.move(participant.character, setup.info.gameRefreshRate, participant.desiredDir)
    participant.copy(character = newChar)
  }

  /**
   * Gestione aggiornamento direzione
   *
   * @param model     modello corrente
   * @param nickname  attore coinvolto
   * @param dir       nuova direzione
   * @param routine   metodo per creazione prossimo Behavior
   * @return          Behavior futuro
   */
  private def updateDesDir(model: Model,
                           nickname: String,
                           dir: Option[Direction],
                           routine: Model => Behavior[EngineCommand]): Behavior[EngineCommand] = {
    val updatePar = model.data.participants.map( par => if(par.nickname == nickname) par.copy(desiredDir = dir) else par )
    routine(model.copy(data = model.data.copy(participants = updatePar)))
  }

  private def clearDesiredDir(model:Model,
                              nickname: String,
                              routine: Model => Behavior[EngineCommand]): Behavior[EngineCommand] =
    updateDesDir(model, nickname, None, routine)

  private def changeDesiredDir(model:Model,
                               nickname: String,
                               move:MoveDirection,
                               routine: Model => Behavior[EngineCommand]): Behavior[EngineCommand] =
    updateDesDir(model, nickname, Some(move), routine)

  /**
   * Procedura per messaggi non gestiti nel Behaviour corrente
   */
  private def unhandledMsg(): Behavior[EngineCommand] = {
    setup.context.log.warn("Ricevuto messaggio non gestito")
    Behaviors.same
  }
}
