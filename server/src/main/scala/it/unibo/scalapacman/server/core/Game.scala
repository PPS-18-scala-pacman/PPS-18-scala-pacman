package it.unibo.scalapacman.server.core

import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.scalapacman.server.core.Game.{CloseCommand, GameCommand, Setup, WakeUp}
import it.unibo.scalapacman.server.util.Settings

object Game {

  sealed trait GameCommand

  case class CloseCommand() extends GameCommand
  case class WakeUp() extends GameCommand

  private case class Setup(id: String, context: ActorContext[GameCommand])

  def apply(id: String): Behavior[GameCommand] =
    Behaviors.setup { context =>

      val GameServiceKey: ServiceKey[GameCommand] = ServiceKey[GameCommand](id)
      context.system.receptionist ! Receptionist.Register(GameServiceKey, context.self)

      Behaviors.withTimers { timers =>

        timers.startTimerWithFixedDelay(WakeUp(), WakeUp(), Settings.gameRefreshRate)
        new Game(Setup(id, context)).game()
      }
    }
}

private class Game(setup: Setup) {

  private def game(): Behavior[GameCommand] = {

    Behaviors.receiveMessage {
      case CloseCommand() =>
        setup.context.log.info("CloseCommand ricevuto")
        Behaviors.stopped
      case WakeUp() =>
        setup.context.log.info("WakeUp id: " + setup.id)
        Behaviors.same
    }
  }
}
