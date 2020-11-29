package it.unibo.scalapacman.server.core

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.scalapacman.lib.model.PacmanType.PacmanType

/**
 * Attore il cui suo scopo è di elaborare le richieste di avvio di nuove partite provvedendo a creare per
 * ognuna di esse un nuovo attore Game.
 */
object Master {

  // Messaggi gestiti dall'attore
  sealed trait MasterCommand
  case class CreateGame(replyTo: ActorRef[GameCreated], components: Map[String, PacmanType]) extends MasterCommand

  // Messaggio di notifica gioco creato
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
      case CreateGame(replyTo, components) =>
        val newCounter = gameCounter + 1
        val gameId = gameIdPrefix format newCounter
        setup.context.spawn(Game(gameId, components), gameId + gameCounter)
        if(replyTo != null) replyTo ! GameCreated(gameId)
        master(setup, newCounter)
    }
}
