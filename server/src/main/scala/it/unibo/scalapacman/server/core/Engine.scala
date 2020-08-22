package it.unibo.scalapacman.server.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.scalapacman.common.DotDTO.rawToDotDTO
import it.unibo.scalapacman.common.FruitDTO.rawToFruitDTO
import it.unibo.scalapacman.common.{DotDTO, FruitDTO, GameEntityDTO, UpdateModelDTO}
import it.unibo.scalapacman.lib.engine.{GameMovement, GameTick}
import it.unibo.scalapacman.lib.engine.GameHelpers.MapHelper
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.GhostType.{BLINKY, CLYDE, GhostType, INKY, PINKY}
import it.unibo.scalapacman.lib.model.{Character, GameObject, GameState, GhostType, Level, LevelState, Map}
import it.unibo.scalapacman.server.core.Engine.{ChangeDirectionCur, ChangeDirectionReq, EngineCommand, Pause,
  RegisterGhost, RegisterPlayer, Resume, Setup, UpdateCommand, UpdateMsg, WakeUp}
import it.unibo.scalapacman.server.model.GameParticipant.gameParticipantToGameEntity
import it.unibo.scalapacman.server.model.MoveDirection.MoveDirection
import it.unibo.scalapacman.server.model.{EngineModel, GameParticipant, Players, RegisteredParticipant, RegistrationModel}
import it.unibo.scalapacman.server.util.Settings

import scala.concurrent.duration.FiniteDuration

object Engine {

  sealed trait EngineCommand

  case class WakeUp() extends EngineCommand
  case class Pause() extends EngineCommand
  case class Resume() extends EngineCommand
  case class ActorRecovery(actorFailed: ActorRef[UpdateCommand]) extends EngineCommand
  case class ChangeDirectionReq(id: ActorRef[UpdateCommand], direction: MoveDirection) extends EngineCommand
  case class ChangeDirectionCur(id: ActorRef[UpdateCommand]) extends EngineCommand

  case class RegisterGhost(actor: ActorRef[UpdateCommand], ghostType: GhostType) extends EngineCommand
  case class RegisterPlayer(actor: ActorRef[UpdateCommand]) extends EngineCommand

  sealed trait UpdateCommand
  case class UpdateMsg(model: UpdateModelDTO) extends UpdateCommand

  private case class Setup(gameId: String, context: ActorContext[EngineCommand], gameRefreshRate: FiniteDuration, level: Int)

  def apply(gameId: String, level: Int): Behavior[EngineCommand] =
    Behaviors.setup { context =>
      new Engine(Setup(gameId, context, Settings.gameRefreshRate, level)).idleRoutine(RegistrationModel())
    }
}

private class Engine(setup: Setup) {

  private def idleRoutine(model: RegistrationModel): Behavior[EngineCommand] =
    Behaviors.receiveMessage {

      case RegisterGhost(actor, ghostType) =>
        val upModel = ghostType match {
          case BLINKY => model.copy(blinky = Some(RegisteredParticipant(actor)))
          case INKY   => model.copy(inky   = Some(RegisteredParticipant(actor)))
          case PINKY  => model.copy(pinky  = Some(RegisteredParticipant(actor)))
          case CLYDE  => model.copy(clyde  = Some(RegisteredParticipant(actor)))
        }
        if(upModel.isFull) {
          mainRoutine( initEngineModel(upModel) )
        } else {
          idleRoutine(upModel)
        }
      case RegisterPlayer(actor) =>
        val upModel = model.copy(pacman = Some(RegisteredParticipant(actor)))
        if(upModel.isFull) {
          mainRoutine( initEngineModel(upModel) )
        } else {
          idleRoutine(upModel)
        }
      case _ => unhandledMsg()
    }

  private def pauseRoutine(model: EngineModel): Behavior[EngineCommand] =
    Behaviors.receiveMessage {
      case Resume() =>
        setup.context.log.info("Resume id: " + setup.gameId)
        mainRoutine(model)
      case Pause() => Behaviors.same
      case _ => unhandledMsg()
    }

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
        case ChangeDirectionCur(actRef) => clearDesiredDir(model, actRef)
        case ChangeDirectionReq(actRef, dir) => changeDesiredDir(model, actRef, dir)
        case _ => unhandledMsg()
      }
    }

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

    EngineModel(players, classicFactory.map, classicFactory.gameState)
  }

  private def updateWatcher(model: EngineModel): Unit =
    model.players.toSet.foreach( _.actRef ! UpdateMsg(elaborateUpdateModel(model)) )

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

    val newMap = GameTick.calculateMap(map)
    var state = GameTick.calculateGameState(GameState(oldModel.state.score) )
    players = GameTick.calculateDeaths(players, state)
    players = GameTick.calculateSpeeds(players, setup.level, state)
    state = GameTick.calculateLevelState(state, players, oldModel.map)

    val model: EngineModel = EngineModel(players, newMap, state)
    updateWatcher(model)

    if(state.levelState == LevelState.ONGOING) {
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

  private def updateDesDir(model:EngineModel, actRef:ActorRef[UpdateCommand], dir:Option[Direction]): Behavior[EngineCommand] = {
    val players = model.players
    val updatePl:Players = actRef match {
      case players.pacman.actRef  => players.copy(pacman  = players.pacman.copy(desiredDir = dir))
      case players.blinky.actRef  => players.copy(blinky  = players.blinky.copy(desiredDir = dir))
      case players.pinky.actRef   => players.copy(pinky   = players.pinky.copy(desiredDir = dir))
      case players.inky.actRef    => players.copy(inky    = players.inky.copy(desiredDir = dir))
      case players.clyde.actRef   => players.copy(clyde   = players.clyde.copy(desiredDir = dir))
    }
    mainRoutine(model.copy(players = updatePl))
  }

  private def clearDesiredDir(model:EngineModel, actRef:ActorRef[UpdateCommand]): Behavior[EngineCommand] =
    updateDesDir(model, actRef, None)

  private def changeDesiredDir(model:EngineModel, actRef:ActorRef[UpdateCommand], move:MoveDirection): Behavior[EngineCommand] =
    updateDesDir(model, actRef, Some(move))

  private def unhandledMsg(): Behavior[EngineCommand] = {
    setup.context.log.warn("Ricevuto messaggio non gestito")
    Behaviors.same
  }
}
