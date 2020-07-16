package it.unibo.scalapacman.server.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.scalapacman.lib.model.GhostType
import it.unibo.scalapacman.server.core.Engine.{EngineCommand, Model, Participant, Pause,
  RegisterGhost, RegisterPlayer, RegisterWatcher, Resume, Setup, UpdateMsg, WakeUp}
import it.unibo.scalapacman.server.util.Settings

object Engine {

  sealed trait EngineCommand
  case class WakeUp() extends EngineCommand
  case class Pause() extends EngineCommand
  case class Resume() extends EngineCommand

  //TODO gestire logica di registrazione e update
  case class RegisterGhost(actor: ActorRef[UpdateCommand], ghostType: GhostType) extends EngineCommand
  case class RegisterPlayer(actor: ActorRef[UpdateCommand]) extends EngineCommand
  case class RegisterWatcher(actor: ActorRef[UpdateCommand]) extends EngineCommand

  private case class Setup(gameId: String, context: ActorContext[EngineCommand])

  //TODO gestire in maniera adeguata la struttura del Model (aggiungere anche wathcer e player)
  private case class Model(blinky: Option[Participant], pinky: Option[Participant], inky: Option[Participant],
                           clyde: Option[Participant], player: Option[Participant])
  //TODO aggiunto Util o def al object model per vedere se è pieno?
  //TODO  a fine registrazione lo converto in un modello senza Option?
  //TODO il model non lo passo alal classe ma ad ogni Routine?

  private case class Participant(actor: ActorRef[UpdateCommand])

  sealed trait UpdateCommand
  case class UpdateMsg(msg: String) extends UpdateCommand

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
    }


  private def pauseRoutine(model: Model): Behavior[EngineCommand] =
    Behaviors.receiveMessage {
      case Resume() =>
        setup.context.log.info("Go id: " + setup.gameId)
        mainRoutine(model)
      case RegisterWatcher(actor) => ???
    }

  private def mainRoutine(model: Model): Behavior[EngineCommand] =
    Behaviors.withTimers { timers =>
      timers.startTimerWithFixedDelay(WakeUp(), WakeUp(), Settings.gameRefreshRate)

      Behaviors.receiveMessage {
        case WakeUp() =>
          setup.context.log.info("WakeUp id: " + setup.gameId)
          //FIXME update di tutti gli osservatori
          if(model.player.isDefined) model.player.get.actor ! UpdateMsg("aggiornamento")
          Behaviors.same
        case Pause() =>
          setup.context.log.info("Pause id: " + setup.gameId)
          pauseRoutine(model)
        case RegisterWatcher(actor) => ???
      }
    }

}
