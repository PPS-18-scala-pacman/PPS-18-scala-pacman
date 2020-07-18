package it.unibo.scalapacman.server.core

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

object Master {

  sealed trait MasterCommand
  case class CreateGame(replyTo: ActorRef[GameCreated]) extends MasterCommand

  case class GameCreated(gameId: String)

  private case class Setup(context: ActorContext[MasterCommand])

  val masterServiceKey: ServiceKey[MasterCommand] = ServiceKey[MasterCommand]("MasterService")

  val gameIdPrefix = "GAME$$n%1$d$$"

  def apply(): Behavior[MasterCommand] =
    Behaviors.setup { context =>
      context.system.receptionist ! Receptionist.Register(Master.masterServiceKey, context.self)
      master(Setup(context), 0)
    }

  private def master(setup: Setup, gameCounter: Int): Behavior[MasterCommand] =
    Behaviors.receiveMessage {
      case CreateGame(replyTo) =>
        val newCounter = gameCounter + 1
        val gameId = gameIdPrefix format newCounter
        setup.context.spawn(Game(gameId), gameId + gameCounter)
        if(replyTo != null) replyTo ! GameCreated(gameId) //FIXME far rispondere direttamente dal game al termine del setup???
        master(setup, newCounter)
    }
}
