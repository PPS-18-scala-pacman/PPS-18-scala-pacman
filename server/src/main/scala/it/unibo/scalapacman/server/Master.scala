package it.unibo.scalapacman.server

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

object Master {

  sealed trait MasterCommand
  case class CreateGame(replyTo: ActorRef[GameCreated]) extends MasterCommand

  case class GameCreated(gameId: String)

  private case class Setup(context: ActorContext[MasterCommand])

  val MasterServiceKey: ServiceKey[MasterCommand] = ServiceKey[MasterCommand]("MasterService")

  val gameIdPrefix = "GAME$$n%1$d$$"

  def apply(): Behavior[MasterCommand] = Behaviors.setup{context =>
    context.system.receptionist ! Receptionist.Register(Master.MasterServiceKey, context.self)
    master(Setup(context), 0)
  }

  private def master(setup: Setup, gameCounter: Int): Behavior[MasterCommand] = {
    Behaviors.receiveMessage {
      case CreateGame(replyTo) =>
        val newCounter = gameCounter + 1
        val gameId = gameIdPrefix format newCounter
        setup.context.spawn(Game(gameId), gameId + gameCounter)
        if(replyTo != null) replyTo ! GameCreated(gameId)
        master(setup, newCounter)
    }
  }
}

