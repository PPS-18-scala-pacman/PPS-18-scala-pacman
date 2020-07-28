package it.unibo.scalapacman.server.core

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.scalapacman.common.{DirectionHolder, DotHolder, FruitHolder, GameCharacter, GameCharacterHolder, GameEntity, Item, Pellet, UpdateModel} // scalastyle:ignore
import it.unibo.scalapacman.lib.engine.{GameMovement, GameTick}
import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.GhostType.{BLINKY, CLYDE, GhostType, INKY, PINKY}
import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit, GameState, Ghost, GhostType, Map, Pacman}
import it.unibo.scalapacman.server.core.Engine.{ChangeDirectionCur, ChangeDirectionReq, EngineCommand, Pause, RegisterGhost, RegisterPlayer, RegisterWatcher, Resume, Setup, UpdateCommand, UpdateMsg, WakeUp} // scalastyle:ignore
import it.unibo.scalapacman.server.model.MoveDirection.MoveDirection
import it.unibo.scalapacman.server.model.{EngineModel, GameParticipant, MoveDirection, Players, RegisteredParticipant, StarterModel} // scalastyle:ignore
import it.unibo.scalapacman.server.util.Settings

import scala.concurrent.duration.FiniteDuration

object Engine {

  sealed trait EngineCommand

  case class WakeUp() extends EngineCommand
  case class Pause() extends EngineCommand
  case class Resume() extends EngineCommand
  case class ChangeDirectionReq(id: ActorRef[UpdateCommand], direction: MoveDirection) extends EngineCommand
  case class ChangeDirectionCur(id: ActorRef[UpdateCommand]) extends EngineCommand

  //TODO gestire logica di registrazione e update
  case class RegisterGhost(actor: ActorRef[UpdateCommand], ghostType: GhostType) extends EngineCommand
  case class RegisterPlayer(actor: ActorRef[UpdateCommand]) extends EngineCommand
  case class RegisterWatcher(actor: ActorRef[UpdateCommand]) extends EngineCommand

  sealed trait UpdateCommand
  case class UpdateMsg(model: UpdateModel) extends UpdateCommand

  private case class Setup(gameId: String, context: ActorContext[EngineCommand], gameRefreshRate: FiniteDuration)

  def apply(gameId: String): Behavior[EngineCommand] =
    Behaviors.setup { context =>
      new Engine(Setup(gameId, context, Settings.gameRefreshRate)).idleRoutine(StarterModel())
    }
}

private class Engine(setup: Setup) {

  private def idleRoutine(model: StarterModel): Behavior[EngineCommand] =
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
      case RegisterWatcher(actor) => ???
    }

  private def pauseRoutine(model: EngineModel): Behavior[EngineCommand] =
    Behaviors.receiveMessage {
      case Resume() =>
        setup.context.log.info("Resume id: " + setup.gameId)
        mainRoutine(model)
      case RegisterWatcher(actor) => ???
      case Pause() => Behaviors.same
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
        case RegisterWatcher(actor) => ???
        case ChangeDirectionCur(actRef) => clearDesiredDir(model, actRef)
        case ChangeDirectionReq(actRef, dir) => changeDesiredDir(model, actRef, dir)
      }
    }

  private def elaborateUpdateModel(model: EngineModel): UpdateModel = {
    //FIXME gestire creazione modello da inviare

    // scalastyle:off magic.number
    //TODO fare trasformatore da model.players a List[GameEntity]
    val gameEntities: List[GameEntity] =
      GameEntity(GameCharacterHolder(GameCharacter.PACMAN), Point2D(1, 2), 1, isDead = false, DirectionHolder(Direction.NORTH)) ::
        GameEntity(GameCharacterHolder(GameCharacter.BLINKY), Point2D(3, 4), 1, isDead = false, DirectionHolder(Direction.NORTH)) ::
        GameEntity(GameCharacterHolder(GameCharacter.INKY), Point2D(5, 6), 1, isDead = false, DirectionHolder(Direction.NORTH)) ::
        Nil

    //TODO creare due metodi nella pacman-lib che data la mappa danno List[Pellet], Option[Fruit]
    val pellets: List[Pellet] =
      Pellet(DotHolder(Dot.SMALL_DOT), Point2D(5, 6)) ::
        Pellet(DotHolder(Dot.SMALL_DOT), Point2D(6, 6)) ::
        Pellet(DotHolder(Dot.SMALL_DOT), Point2D(7, 6)) ::
        Pellet(DotHolder(Dot.SMALL_DOT), Point2D(8, 6)) ::
        Nil
    val fruit = Some(Item(FruitHolder(Fruit.APPLE), Point2D(9, 9)))
    // scalastyle:on magic.number

    UpdateModel(gameEntities, model.state, pellets, fruit)
  }

  private def initEngineModel(startMod: StarterModel) = {

    // scalastyle:off magic.number
    //FIXME Gio aggiungerà metodi di creazione con default
    val players = Players(
      pacman = GameParticipant(Pacman(Point2D(1, 0), 1.0, Direction.SOUTH), startMod.pacman.get.actor),
      blinky = GameParticipant(Ghost(GhostType.BLINKY, Point2D(9, 9), 0.9, Direction.EAST), startMod.blinky.get.actor),
      clyde  = GameParticipant(Ghost(GhostType.CLYDE, Point2D(9, 9), 0.9, Direction.EAST), startMod.clyde.get.actor),
      inky   = GameParticipant(Ghost(GhostType.INKY, Point2D(9, 9), 0.9, Direction.EAST), startMod.inky.get.actor),
      pinky  = GameParticipant(Ghost(GhostType.PINKY, Point2D(9, 9), 0.9, Direction.EAST), startMod.pinky.get.actor),
    )
    // scalastyle:on magic.number

    EngineModel(players, Map.classic, GameState(score = 0))
  }

  private def updateWatcher(model: EngineModel): Unit =
    model.players.toSeq.foreach( _.actRef ! UpdateMsg(elaborateUpdateModel(model)) )

  private def updateGame(oldModel: EngineModel) : Behavior[EngineCommand] = {
    setup.context.log.info("updateGame id: " + setup.gameId)
    implicit val map: Map = oldModel.map

    val pacman = updateChar(oldModel.players.pacman)
    val pinky = updateChar(oldModel.players.pinky)
    val inky = updateChar(oldModel.players.inky)
    val clyde = updateChar(oldModel.players.clyde)
    val blinky = updateChar(oldModel.players.blinky)
    val players = Players(pacman = pacman, pinky = pinky, inky = inky, clyde = clyde, blinky = blinky)

    val characters = pacman.character :: pinky.character :: inky.character :: clyde.character :: blinky.character :: Nil

    val collisions = GameTick.collisions(characters)
    val state = GameTick.calculateGameState(collisions)

    //TODO aggiunrere calcolo personaggi vivi, update della mappa, calcolo delle velocità
    // da valutare: calcolo direzioni fantasmi(per energizer pacman)

    val model: EngineModel = oldModel.copy(players = players, state = state)
    updateWatcher(model)
    mainRoutine(model)
  }

  private def updateChar(participant: GameParticipant)(implicit map: Map): GameParticipant = {
    val newChar = GameMovement.move(participant.character, setup.gameRefreshRate, participant.desiredDir)
    participant.copy(character = newChar)
  }

  private def updateDesDir(model:EngineModel, actRef:ActorRef[UpdateCommand], dir:Option[Direction]): Behavior[EngineCommand] = {
    val players = model.players
    val updatePl:Players = actRef match {
      case players.blinky.actRef => players.copy(blinky = players.blinky.copy(desiredDir = dir))
      case players.blinky.actRef => players.copy(blinky = players.blinky.copy(desiredDir = dir))
      case players.blinky.actRef => players.copy(blinky = players.blinky.copy(desiredDir = dir))
      case players.blinky.actRef => players.copy(blinky = players.blinky.copy(desiredDir = dir))
      case players.blinky.actRef => players.copy(blinky = players.blinky.copy(desiredDir = dir))
    }
    mainRoutine(model.copy(players = updatePl))
  }

  private def clearDesiredDir(model:EngineModel, actRef:ActorRef[UpdateCommand]): Behavior[EngineCommand] =
    updateDesDir(model, actRef, None)

  private def changeDesiredDir(model:EngineModel, actRef:ActorRef[UpdateCommand], move:MoveDirection): Behavior[EngineCommand] = {
    val dir: Direction = move match {
      case MoveDirection.UP     => Direction.NORTH
      case MoveDirection.DOWN   => Direction.SOUTH
      case MoveDirection.LEFT   => Direction.WEST
      case MoveDirection.RIGHT  => Direction.EAST
    }
    updateDesDir(model, actRef, Some(dir))
  }
}
