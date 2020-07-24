package it.unibo.scalapacman.server.core

import java.io.StringWriter

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import it.unibo.scalapacman.common.UpdateModel
import it.unibo.scalapacman.server.core.Engine.UpdateMsg
import it.unibo.scalapacman.server.core.Player.{PlayerCommand, RegisterUser, RegistrationAccepted, RegistrationRejected, Setup, WrapRespMessage, WrapRespUpdate}

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

  setup.engine ! Engine.RegisterPlayer(updateMsgAdapter)

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  private def initRoutine(): Behavior[PlayerCommand] =
    Behaviors.receiveMessage {
      case RegisterUser(replyTo, sourceAct) =>
        replyTo ! RegistrationAccepted(clientMsgAdapter)
        mainRoutine(sourceAct)
      case WrapRespMessage(TextMessage.Strict(msg)) =>
        setup.context.log.info("Ricevuto messaggio: " + msg)
        Behaviors.same
      case WrapRespMessage(_) =>
        setup.context.log.warn("Ricevuto messaggio non gestito")
        Behaviors.same
      case WrapRespUpdate(UpdateMsg(updateMsg)) =>
        setup.context.log.info("Ricevuto update: " + updateMsg)
        Behaviors.same
      case WrapRespUpdate(_) =>
        setup.context.log.warn("Ricevuto update non gestito")
        Behaviors.same
    }

  private def mainRoutine(sourceAct: ActorRef[Message]): Behavior[PlayerCommand] =
    Behaviors.receiveMessage {
      //TODO gestire messaggi client ed engine
      case RegisterUser(replyTo, _) =>
        replyTo ! RegistrationRejected("Player occupato") //FIXME
        Behaviors.same
      case WrapRespMessage(TextMessage.Strict(msg)) =>
        setup.context.log.info("Ricevuto messaggio: " + msg)
        Behaviors.same
      case WrapRespMessage(_) =>
        setup.context.log.warn("Ricevuto messaggio non gestito")
        Behaviors.same
      case WrapRespUpdate(UpdateMsg(model)) =>
        setup.context.log.debug("Ricevuto update: " + model)
        val msg = convertModel(model)
        sourceAct ! TextMessage(msg)
        Behaviors.same
      case WrapRespUpdate(_) =>
        setup.context.log.warn("Ricevuto update non gestito")
        Behaviors.same
    }

  private def convertModel(model: UpdateModel): String = {
    val out = new StringWriter
    mapper.writeValue(out, model)
    out.toString
  }
}
