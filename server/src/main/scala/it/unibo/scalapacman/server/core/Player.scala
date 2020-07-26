package it.unibo.scalapacman.server.core

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import it.unibo.scalapacman.server.core.Player.{PlayerCommand, RegisterUser, RegistrationAccepted, RegistrationRejected,
  Setup, WrapRespMessage, WrapRespUpdate}
import it.unibo.scalapacman.server.util.ConversionUtils

object Player {

  sealed trait PlayerCommand
  case class RegisterUser(replyTo: ActorRef[PlayerRegistration], sourceAct: ActorRef[Message]) extends PlayerCommand

  case class WrapRespMessage(response: Message) extends PlayerCommand
  case class WrapRespUpdate(response: Engine.UpdateCommand) extends PlayerCommand

  sealed trait PlayerRegistration
  case class RegistrationAccepted(messageHandler: ActorRef[Message]) extends PlayerRegistration
  case class RegistrationRejected(cause: String) extends PlayerRegistration

  private case class Setup(gameId: String,
                           context: ActorContext[PlayerCommand],
                           engine: ActorRef[Engine.EngineCommand])

  def apply(gameId: String, engine: ActorRef[Engine.EngineCommand]): Behavior[PlayerCommand] =
    Behaviors.setup { context =>
      new Player(Setup(gameId, context, engine)).initRoutine()
    }
}

class Player(setup: Setup) {

  val clientMsgAdapter: ActorRef[Message] = setup.context.messageAdapter(WrapRespMessage)
  val updateMsgAdapter: ActorRef[Engine.UpdateCommand] = setup.context.messageAdapter(WrapRespUpdate)

  private def initRoutine(): Behavior[PlayerCommand] =
    Behaviors.receiveMessage {
      case RegisterUser(replyTo, sourceAct) =>
        setup.engine ! Engine.RegisterPlayer(updateMsgAdapter)
        replyTo ! RegistrationAccepted(clientMsgAdapter)
        mainRoutine(sourceAct)
      case WrapRespUpdate(Engine.UpdateMsg(updateMsg)) =>
        setup.context.log.info("Ricevuto update: " + updateMsg)
        Behaviors.same
      case _ =>
        setup.context.log.warn("Ricevuto messaggio non gestito")
        Behaviors.same
    }

  private def mainRoutine(sourceAct: ActorRef[Message]): Behavior[PlayerCommand] =
    Behaviors.receiveMessage {
      case RegisterUser(replyTo, _) =>
        replyTo ! RegistrationRejected("Player occupato")
        Behaviors.same
      case WrapRespMessage(TextMessage.Strict(msg)) =>
        setup.context.log.info("Ricevuto messaggio: " + msg)
        val command = ConversionUtils.convertClientMsg(msg, updateMsgAdapter)
        if(command.isDefined) setup.engine ! command.get
        Behaviors.same
      case WrapRespUpdate(Engine.UpdateMsg(model)) =>
        setup.context.log.debug("Ricevuto update: " + model)
        val msg = ConversionUtils.convertModel(model)
        sourceAct ! TextMessage(msg)
        Behaviors.same
      case _ =>
        setup.context.log.warn("Ricevuto messaggio non gestito")
        Behaviors.same
    }
}
