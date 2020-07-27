package it.unibo.scalapacman.server.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.scalapacman.common.{DirectionHolder, DotHolder, FruitHolder, GameCharacter, GameCharacterHolder}
import it.unibo.scalapacman.common.{GameEntity, GameState, Item, Pellet, UpdateModel}
import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit, GhostType}
import it.unibo.scalapacman.server.core.Engine.MoveDirection.MoveDirection
import it.unibo.scalapacman.server.core.Engine.{ChangeDirectionCur, ChangeDirectionReq, EngineCommand,
  Model, Participant, Pause, RegisterGhost, RegisterPlayer, RegisterWatcher, Resume, Setup,
  SwitchGameState, UpdateMsg, WakeUp}
import it.unibo.scalapacman.server.util.Settings

object Engine {

  sealed trait EngineCommand
  case class WakeUp() extends EngineCommand
  case class Pause() extends EngineCommand
  case class Resume() extends EngineCommand

  // TODO valutare questa gestione che non mi fa impazzire
  sealed trait GameEntityCommand extends EngineCommand
  case class SwitchGameState() extends GameEntityCommand
  sealed trait DirectionCommand extends GameEntityCommand
  case class ChangeDirectionReq(id:ActorRef[UpdateCommand], direction:MoveDirection) extends DirectionCommand
  case class ChangeDirectionCur(id:ActorRef[UpdateCommand]) extends DirectionCommand


  //FIXME SPOSTARE IN PACMAN-LIB???
  object MoveDirection extends Enumeration {
    type MoveDirection = Value
    val UP, DOWN, RIGHT, LEFT = Value
  }

  //TODO gestire logica di registrazione e update
  case class RegisterGhost(actor: ActorRef[UpdateCommand], ghostType: GhostType) extends EngineCommand
  case class RegisterPlayer(actor: ActorRef[UpdateCommand]) extends EngineCommand
  case class RegisterWatcher(actor: ActorRef[UpdateCommand]) extends EngineCommand

  private case class Setup(gameId: String, context: ActorContext[EngineCommand])

  //TODO gestire in maniera adeguata la struttura del Model (aggiungere anche wathcer e player)
  private case class Model(blinky: Option[Participant], pinky: Option[Participant], inky: Option[Participant],
                           clyde: Option[Participant], player: Option[Participant])
  //TODO aggiunto Util o def al object model per vedere se è pieno?
  //TODO a fine registrazione lo converto in un modello senza Option?
  //TODO il model non lo passo alal classe ma ad ogni Routine?

  private case class Participant(actor: ActorRef[UpdateCommand])

  sealed trait UpdateCommand
  case class UpdateMsg(model: UpdateModel) extends UpdateCommand

  def apply(gameId: String): Behavior[EngineCommand] =
    Behaviors.setup { context =>
      new Engine(Setup(gameId, context)).idleRoutine()
    }
}

private class Engine(setup: Setup) {

  private def idleRoutine(): Behavior[EngineCommand] =
    Behaviors.receiveMessage {
      //TODO ad ogni registrazione guardo se sono arrivati tutti nel caso cambio stato
      case RegisterGhost(actor, ghostType) => ???
      case RegisterPlayer(actor) =>
        //FIXME lo logica va corretta
        mainRoutine(Model(Option.empty, Option.empty, None, Option.empty, Some(Participant(actor))))
      case RegisterWatcher(actor) => ???
      case SwitchGameState() => ???
    }


  private def pauseRoutine(model: Model): Behavior[EngineCommand] =
    Behaviors.receiveMessage {
      case Resume() =>
        setup.context.log.info("Go id: " + setup.gameId)
        mainRoutine(model)
      case RegisterWatcher(actor) => ???
      case SwitchGameState() => ???
    }

  private def mainRoutine(model: Model): Behavior[EngineCommand] =
    Behaviors.withTimers { timers =>
      timers.startTimerWithFixedDelay(WakeUp(), WakeUp(), Settings.gameRefreshRate)

      Behaviors.receiveMessage {
        case WakeUp() =>
          setup.context.log.info("WakeUp id: " + setup.gameId)
          //FIXME update di tutti gli osservatori
          if(model.player.isDefined) model.player.get.actor ! UpdateMsg(elaborateModel())
          Behaviors.same
        case Pause() =>
          setup.context.log.info("Pause id: " + setup.gameId)
          pauseRoutine(model)
        case RegisterWatcher(actor) => ???
        case ChangeDirectionCur(_) => ???
        case ChangeDirectionReq(_, _) => ???
        case SwitchGameState() => ???
      }
    }

  private def elaborateModel(): UpdateModel = {
    //FIXME gestire creazione modello da inviare

    // scalastyle:off magic.number
    val gameEntities:List[GameEntity] =
      GameEntity(GameCharacterHolder(GameCharacter.PACMAN), Point2D(1,2), isDead=false, DirectionHolder(Direction.NORTH)) ::
        GameEntity(GameCharacterHolder(GameCharacter.PACMAN), Point2D(3,4), isDead=false, DirectionHolder(Direction.NORTH)) ::
        GameEntity(GameCharacterHolder(GameCharacter.PACMAN), Point2D(5,6), isDead=false, DirectionHolder(Direction.NORTH)) ::
        Nil
    val gs: GameState = GameState(ghostInFear=false, pacmanEmpowered=false)
    val pellets: List[Pellet] =
      Pellet(DotHolder(Dot.SMALL_DOT), Point2D(5,6)) ::
        Pellet(DotHolder(Dot.SMALL_DOT), Point2D(6,6)) ::
        Pellet(DotHolder(Dot.SMALL_DOT), Point2D(7,6)) ::
        Pellet(DotHolder(Dot.SMALL_DOT), Point2D(8,6)) ::
        Nil
    val fruit = Some(Item(FruitHolder(Fruit.APPLE), Point2D(9,9)))
    // scalastyle:on magic.number

    UpdateModel(gameEntities, 2, gs, pellets, fruit)
  }
}
