package it.unibo.scalapacman.server.core

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.scalapacman.common._
import it.unibo.scalapacman.lib.engine.{GameMovement, GameTick}
import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.Direction._
import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit, GameState, Ghost, GhostType, Map, Pacman}
import it.unibo.scalapacman.server.core.Engine._
import it.unibo.scalapacman.server.model.MoveDirection._
import it.unibo.scalapacman.server.model.{EngineModel, GameParticipant, Players, StarterModel}
import it.unibo.scalapacman.server.util.Settings

import scala.concurrent.duration.FiniteDuration

object Engine {

  def apply(gameId: String): Behavior[EngineCommand] =
    Behaviors.setup { context =>
      new Engine(Setup(gameId, context, Settings.gameRefreshRate)).idleRoutine(StarterModel())
    }

  sealed trait EngineCommand

  // TODO valutare questa gestione che non mi fa impazzire
  sealed trait GameEntityCommand extends EngineCommand

  sealed trait DirectionCommand extends GameEntityCommand

  sealed trait UpdateCommand

  case class WakeUp() extends EngineCommand

  case class Pause() extends EngineCommand

  case class Resume() extends EngineCommand

  case class SwitchGameState() extends GameEntityCommand

  case class ChangeDirectionReq(id: ActorRef[UpdateCommand], direction: MoveDirection) extends DirectionCommand

  case class ChangeDirectionCur(id: ActorRef[UpdateCommand]) extends DirectionCommand

  //TODO gestire logica di registrazione e update
  case class RegisterGhost(actor: ActorRef[UpdateCommand], ghostType: GhostType) extends EngineCommand

  case class RegisterPlayer(actor: ActorRef[UpdateCommand]) extends EngineCommand

  //TODO aggiunto Util o def al object model per vedere se è pieno?

  case class RegisterWatcher(actor: ActorRef[UpdateCommand]) extends EngineCommand

  case class UpdateMsg(model: UpdateModel) extends UpdateCommand

  private case class Setup(gameId: String, context: ActorContext[EngineCommand], gameRefreshRate: FiniteDuration)

}

private class Engine(setup: Setup) {

  private def idleRoutine(model: StarterModel): Behavior[EngineCommand] =
    Behaviors.receiveMessage {
      //TODO ad ogni registrazione guardo se sono arrivati tutti nel caso cambio stato
      case RegisterGhost(actor, ghostType) => ???
      case RegisterPlayer(actor) =>

        //FIXME  cambiare stato solo dopo che tutti i ghost e il player si sono registrati e crere il EngineModel
        //TODO aggiungere StarterModel un metodo isComplete???
        mainRoutine( initEngineModel(model) )
      case RegisterWatcher(actor) => ???
      case SwitchGameState() => ???
    }


  private def pauseRoutine(model: EngineModel): Behavior[EngineCommand] =
    Behaviors.receiveMessage {
      case Resume() =>
        setup.context.log.info("Go id: " + setup.gameId)
        mainRoutine(model)
      case RegisterWatcher(actor) => ???
      case SwitchGameState() => ???
    }

  private def mainRoutine(model: EngineModel): Behavior[EngineCommand] =
    Behaviors.withTimers { timers =>
      timers.startTimerWithFixedDelay(WakeUp(), WakeUp(), setup.gameRefreshRate)

      Behaviors.receiveMessage {
        case WakeUp() =>
          updateGame(model)
        case Pause() =>
          setup.context.log.info("Pause id: " + setup.gameId)
          pauseRoutine(model)
        case RegisterWatcher(actor) => ???
        case ChangeDirectionCur(actRef) => clearDesiredDir(model, actRef)
        case ChangeDirectionReq(actRef, dir) => changeDesiredDir(model, actRef, dir)
        case SwitchGameState() => ???
      }
    }

  private def elaborateUpdateModel(model: EngineModel): UpdateModel = {
    //FIXME gestire creazione modello da inviare

    // scalastyle:off magic.number
    //TODO fare trasformatore da model.players a List[GameEntity]
    val gameEntities: List[GameEntity] =
      GameEntity(GameCharacterHolder(GameCharacter.PACMAN), Point2D(1, 2), isDead = false, DirectionHolder(Direction.NORTH)) ::
        GameEntity(GameCharacterHolder(GameCharacter.BLINKY), Point2D(3, 4), isDead = false, DirectionHolder(Direction.NORTH)) ::
        GameEntity(GameCharacterHolder(GameCharacter.INKY), Point2D(5, 6), isDead = false, DirectionHolder(Direction.NORTH)) ::
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

  private def updateWatcher(model: EngineModel): Unit = {
    val upMsg = UpdateMsg(elaborateUpdateModel(model))

    //TODO fare un implicit che aggiunge metodo foreach a Player?
    model.players.blinky.actRef ! upMsg
    model.players.clyde.actRef ! upMsg
    model.players.inky.actRef ! upMsg
    model.players.pinky.actRef ! upMsg
    model.players.pacman.actRef ! upMsg
  }

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
      case UP     => NORTH
      case DOWN   => SOUTH
      case LEFT   => WEST
      case RIGHT  => EAST
    }
    updateDesDir(model, actRef, Some(dir))
  }
}
