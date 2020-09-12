package it.unibo.scalapacman.server.core

import akka.actor.typed.scaladsl.TimerScheduler
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.scalapacman.common.DotDTO.rawToDotDTO
import it.unibo.scalapacman.common.FruitDTO.rawToFruitDTO
import it.unibo.scalapacman.common.GameCharacter.{GameCharacter, PACMAN}
import it.unibo.scalapacman.common.{DotDTO, FruitDTO, GameEntityDTO, UpdateModelDTO}
import it.unibo.scalapacman.lib.engine.{GameMovement, GameTick}
import it.unibo.scalapacman.lib.engine.GameHelpers.MapHelper
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.lib.model.{Character, GameObject, GhostType, Level, LevelState, Map}
import it.unibo.scalapacman.server.core.Engine.{ActorRecovery, ChangeDirectionCur, ChangeDirectionReq, EngineCommand,
  Pause, RegisterGhost, RegisterPlayer, Resume, Setup, UpdateCommand, UpdateMsg, WakeUp}
import it.unibo.scalapacman.server.model.GameParticipant.gameParticipantToGameEntity
import it.unibo.scalapacman.server.model.MoveDirection.MoveDirection
import it.unibo.scalapacman.server.model.{EngineModel, GameParticipant, Players, RegistrationModel}
import it.unibo.scalapacman.server.config.Settings
import it.unibo.scalapacman.server.util.{RecoveryHelper, RegistrationHelper}

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
  case class Resume() extends EngineCommand
  case class ActorRecovery(entityFailed: GameCharacter) extends EngineCommand
  case class ChangeDirectionReq(id: ActorRef[UpdateCommand], direction: MoveDirection) extends EngineCommand
  case class ChangeDirectionCur(id: ActorRef[UpdateCommand]) extends EngineCommand

  case class RegisterGhost(actor: ActorRef[UpdateCommand], ghostType: GhostType) extends EngineCommand
  case class RegisterPlayer(actor: ActorRef[UpdateCommand]) extends EngineCommand

  // Messaggi inviati agli osservatori
  sealed trait UpdateCommand
  case class UpdateMsg(model: UpdateModelDTO) extends UpdateCommand

  private case class Setup(gameId: String, context: ActorContext[EngineCommand], gameRefreshRate: FiniteDuration, pauseRefreshRate: FiniteDuration, level: Int)

  def apply(gameId: String, level: Int): Behavior[EngineCommand] =
    Behaviors.setup { context =>
      new Engine(Setup(gameId, context, Settings.gameRefreshRate, Settings.pauseRefreshRate, level)).idleRoutine(RegistrationModel())
    }
}

private class Engine(setup: Setup) {

  /**
   * Behavior iniziale, gestisce la prima registrazione da parte degli attori partecipanti alla partita
   */
  private def idleRoutine(model: RegistrationModel): Behavior[EngineCommand] =
    Behaviors.receiveMessage {

      case RegisterGhost(actor, ghostType) => handleRegistration(model, ghostType, actor)

      case RegisterPlayer(actor) => handleRegistration(model, PACMAN, actor)

      case ActorRecovery(charType) =>
        val upModel = RegistrationHelper.unRegisterPartecipant(model, charType)
        idleRoutine(upModel)

      case _ => unhandledMsg()
    }

  /**
   * Behavior predisposto alla gestione di eventuali guasti da parte degli attori che partecipano alla partita
   */
  private def recoveryRoutine(regModel: RegistrationModel, engModel:EngineModel): Behavior[EngineCommand] =
    Behaviors.receiveMessage {
      case RegisterGhost(actor, ghostType) =>
        val (upRegModel, upEngModel) = RecoveryHelper.replacePartecipant(regModel, engModel, ghostType, actor)
        if(upRegModel.isFull) pauseRoutine(upEngModel) else recoveryRoutine(upRegModel, upEngModel)

      case RegisterPlayer(actor) =>
        val (upRegModel, upEngModel) = RecoveryHelper.replacePartecipant(regModel, engModel, PACMAN, actor)
        if(upRegModel.isFull) pauseRoutine(upEngModel) else recoveryRoutine(upRegModel, upEngModel)

      case ActorRecovery(charType) =>
        val upModel = RegistrationHelper.unRegisterPartecipant(regModel, charType)
        recoveryRoutine(upModel, engModel)

      case _ => unhandledMsg()
    }

  /**
   * Behavior utilizzato nel caso in cui il gioco venga messo in pausa, esegue l'invio di aggiornamenti
   * senza procedere all'aggiornamento dello stato della partita
   */
  private def pauseRoutine(model: EngineModel): Behavior[EngineCommand] = {
    Behaviors.withTimers { timers =>
      timers.startTimerWithFixedDelay(WakeUp(), WakeUp(), setup.pauseRefreshRate)

      Behaviors.receiveMessage {
        case WakeUp() =>
          updateWatcher(model)
          Behaviors.same
        case Resume() =>
          setup.context.log.info("Resume id: " + setup.gameId)
          timers.cancel(WakeUp())
          mainRoutine(model)
        case Pause() => Behaviors.same
        case ChangeDirectionCur(actRef) => clearDesiredDir(model, actRef, pauseRoutine)
        case ChangeDirectionReq(actRef, dir) => changeDesiredDir(model, actRef, dir, pauseRoutine)
        case ActorRecovery(charType) => prepareRecovery(model, charType, timers)
        case _ => unhandledMsg()
      }
    }
  }

  /**
   * Behavior utilizzato durante il corso della partita, effettua il ricalcolo periodico dello stato della
   * partita e si occupa di aggiornare gli osservatori, gestisce le richieste di pausa e cambio direzione
   */
  private def mainRoutine(model: EngineModel): Behavior[EngineCommand] =
    Behaviors.withTimers { timers =>
      timers.startTimerWithFixedDelay(WakeUp(), WakeUp(), setup.gameRefreshRate)

      Behaviors.receiveMessage {
        case WakeUp() =>
          updateGame(model)
        case Pause() =>
          setup.context.log.info("Pause id: " + setup.gameId)
          timers.cancel(WakeUp())
          pauseRoutine(model)
        case ChangeDirectionCur(actRef) => clearDesiredDir(model, actRef, mainRoutine)
        case ChangeDirectionReq(actRef, dir) => changeDesiredDir(model, actRef, dir, mainRoutine)
        case ActorRecovery(charType) => prepareRecovery(model, charType, timers)
        case _ => unhandledMsg()
      }
    }

  private def handleRegistration(registrationModel: RegistrationModel,
                                 charType: GameCharacter,
                                 actor: ActorRef[UpdateCommand]): Behavior[EngineCommand] = {
    val upModel = RegistrationHelper.registerPartecipant(registrationModel, charType, actor)
    if(upModel.isFull) {
      val engModel = initEngineModel(upModel)
      updateWatcher(engModel)
      pauseRoutine(engModel)
    } else {
      idleRoutine(upModel)
    }
  }

  private def prepareRecovery(engineModel: EngineModel,
                              charType: GameCharacter,
                              timers: TimerScheduler[EngineCommand]): Behavior[EngineCommand] = {
    timers.cancel(WakeUp())
    recoveryRoutine(RecoveryHelper.createRecoveryModel(charType, engineModel), engineModel)
  }

  /**
   * Crea il dto contentente lo stato della partita a partire dal modello di gioco
   *
   * @param model  modello contenente lo stato corrente del gioco
   * @return       dto per aggiornamento osservatori
   */
  private def elaborateUpdateModel(model: EngineModel): UpdateModelDTO = {

    val gameEntities: Set[GameEntityDTO] = model.players.toSet.map(gameParticipantToGameEntity)
    val dots: Set[DotDTO]                = model.map.dots.map(rawToDotDTO).toSet
    val fruit: Option[FruitDTO]          = model.map.fruit.map(rawToFruitDTO)

    UpdateModelDTO(gameEntities, model.state, dots, fruit)
  }

  private def initEngineModel(startMod: RegistrationModel) = {

    val classicFactory = Level.Classic(setup.level)
    val players = Players(
      pacman = GameParticipant(classicFactory.pacman, startMod.pacman.get.actor),
      blinky = GameParticipant(classicFactory.ghost(GhostType.BLINKY), startMod.blinky.get.actor),
      clyde  = GameParticipant(classicFactory.ghost(GhostType.CLYDE), startMod.clyde.get.actor),
      inky   = GameParticipant(classicFactory.ghost(GhostType.INKY), startMod.inky.get.actor),
      pinky  = GameParticipant(classicFactory.ghost(GhostType.PINKY), startMod.pinky.get.actor),
    )

    EngineModel(players, classicFactory.map, classicFactory.gameState, classicFactory.gameEvents)
  }

  private def updateWatcher(model: EngineModel): Unit =
    model.players.toSet.foreach( _.actRef ! UpdateMsg(elaborateUpdateModel(model)) )

  /**
   * Calcolo del nuovo stato di gioco a partire dal precedente
   */
  private def updateGame(oldModel: EngineModel) : Behavior[EngineCommand] = {
    setup.context.log.debug("updateGame id: " + setup.gameId)
    implicit val map: Map = oldModel.map

    val pacman  = updateChar(oldModel.players.pacman)
    val pinky   = updateChar(oldModel.players.pinky)
    val inky    = updateChar(oldModel.players.inky)
    val clyde   = updateChar(oldModel.players.clyde)
    val blinky  = updateChar(oldModel.players.blinky)
    implicit var players: Players = Players(pacman = pacman, pinky = pinky, inky = inky, clyde = clyde, blinky = blinky)

    implicit val collisions: List[(Character, GameObject)] = GameTick.collisions(players)

    var newMap = GameTick.calculateMap(map)
    var state = GameTick.calculateGameState(oldModel.state)
    players = GameTick.calculateDeaths(players, state)
    players = GameTick.calculateSpeeds(players, setup.level, state)
    state = GameTick.calculateLevelState(state, players, oldModel.map)

    var gameEvents = oldModel.gameEvents
    gameEvents = GameTick.consumeTimeOfGameEvents(gameEvents, setup.gameRefreshRate)
    gameEvents = GameTick.updateEvents(gameEvents, state, collisions)
    players = GameTick.handleEvents(gameEvents, players)
    newMap = GameTick.handleEvents(gameEvents, newMap)
    state = GameTick.handleEvents(gameEvents, state)
    gameEvents = GameTick.removeTimedOutGameEvents(gameEvents)

    val model: EngineModel = EngineModel(players, newMap, state, gameEvents)
    updateWatcher(model)

    if (state.levelState == LevelState.ONGOING) {
      mainRoutine(model)
    } else {
      setup.context.log.info("Partita terminata spegnimento")
      Behaviors.stopped
    }
  }

  private def updateChar(participant: GameParticipant)(implicit map: Map): GameParticipant = {
    val newChar = GameMovement.move(participant.character, setup.gameRefreshRate, participant.desiredDir)
    participant.copy(character = newChar)
  }

  /**
   * Gestione aggiornamento direzione
   *
   * @param model   modello corrente
   * @param actRef  attore coinvolto
   * @param dir     nuova direzione
   * @param routine meotdo per creazione prossimo Behavior
   * @return        Behavior futuro
   */
  private def updateDesDir(model:EngineModel,
                           actRef:ActorRef[UpdateCommand],
                           dir:Option[Direction],
                           routine: EngineModel => Behavior[EngineCommand]): Behavior[EngineCommand] = {
    val players = model.players
    val updatePl:Players = actRef match {
      case players.pacman.actRef  => players.copy(pacman  = players.pacman.copy(desiredDir = dir))
      case players.blinky.actRef  => players.copy(blinky  = players.blinky.copy(desiredDir = dir))
      case players.pinky.actRef   => players.copy(pinky   = players.pinky.copy(desiredDir = dir))
      case players.inky.actRef    => players.copy(inky    = players.inky.copy(desiredDir = dir))
      case players.clyde.actRef   => players.copy(clyde   = players.clyde.copy(desiredDir = dir))
    }
    routine(model.copy(players = updatePl))
  }

  private def clearDesiredDir(model:EngineModel,
                              actRef:ActorRef[UpdateCommand],
                              routine: EngineModel => Behavior[EngineCommand]): Behavior[EngineCommand] =
    updateDesDir(model, actRef, None, routine)

  private def changeDesiredDir(model:EngineModel,
                               actRef:ActorRef[UpdateCommand],
                               move:MoveDirection,
                               routine: EngineModel => Behavior[EngineCommand]): Behavior[EngineCommand] =
    updateDesDir(model, actRef, Some(move), routine)

  /**
   * Procedura per messaggi non gestiti nel Behaviour corrente
   */
  private def unhandledMsg(): Behavior[EngineCommand] = {
    setup.context.log.warn("Ricevuto messaggio non gestito")
    Behaviors.same
  }
}
