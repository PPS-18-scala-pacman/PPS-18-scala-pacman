package it.unibo.scalapacman.server.core

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import it.unibo.scalapacman.common.{Command, CommandType, CommandTypeHolder, JSONConverter, MoveCommandType, MoveCommandTypeHolder}
import it.unibo.scalapacman.server.communication.ConnectionProtocol.{ConnectionAck, ConnectionData, ConnectionEnded,
  ConnectionFailed, ConnectionInit, ConnectionMsg}
import it.unibo.scalapacman.server.core.Engine.UpdateCommand
import it.unibo.scalapacman.server.core.Player.{PlayerCommand, RegisterUser, RegistrationAccepted, RegistrationRejected, Setup, WrapRespMessage, WrapRespUpdate}
import it.unibo.scalapacman.server.model.MoveDirection

object Player {

  sealed trait PlayerCommand
  case class RegisterUser(replyTo: ActorRef[PlayerRegistration], sourceAct: ActorRef[Message]) extends PlayerCommand

  case class WrapRespMessage(response: ConnectionMsg) extends PlayerCommand
  case class WrapRespUpdate(response: Engine.UpdateCommand) extends PlayerCommand

  sealed trait PlayerRegistration
  case class RegistrationAccepted(messageHandler: ActorRef[ConnectionMsg]) extends PlayerRegistration
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

  val clientMsgAdapter: ActorRef[ConnectionMsg] = setup.context.messageAdapter(WrapRespMessage)
  val updateMsgAdapter: ActorRef[Engine.UpdateCommand] = setup.context.messageAdapter(WrapRespUpdate)

  private def initRoutine(): Behavior[PlayerCommand] =
    Behaviors.receiveMessage {
      case RegisterUser(replyTo, sourceAct) =>
        setup.engine ! Engine.RegisterPlayer(updateMsgAdapter)
        replyTo ! RegistrationAccepted(clientMsgAdapter)
        mainRoutine(sourceAct)
      case WrapRespUpdate(Engine.UpdateMsg(updateMsg)) =>
        setup.context.log.warn("Ricevuto update: " + updateMsg)
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
      case WrapRespUpdate(Engine.UpdateMsg(model)) =>
        setup.context.log.debug("Ricevuto update: " + model)
        val msg = JSONConverter.toJSON(model)
        sourceAct ! TextMessage(msg)
        Behaviors.same
      case WrapRespMessage(ConnectionData(act, TextMessage.Strict(msg))) =>
        setup.context.log.debug("Ricevuto messaggio: " + msg)
        val command = JSONConverter.fromJSON[Command](msg) flatMap (parseClientCommand(_, updateMsgAdapter))
        if(command.isDefined) setup.engine ! command.get
        act ! ConnectionAck()
        Behaviors.same
      case WrapRespMessage(ConnectionInit(act)) =>
        setup.context.log.info("Ricevuto messaggio connessione instaurata")
        act ! ConnectionAck()
        Behaviors.same
      case WrapRespMessage(ConnectionEnded()) =>
        setup.context.log.info("Ricevuto messaggio connessione chiusa")
        Behaviors.stopped
      case WrapRespMessage(ConnectionFailed(ex)) =>
        setup.context.log.error(s"Ricevuto messaggio connessione fallita ${ex.getMessage}")
        setup.context.log.debug(ex.getStackTrace.toString)
        throw ex
      case msg:Any =>
        setup.context.log.warn(s"Ricevuto messaggio non gestito: $msg")
        Behaviors.same
    }

  private def parseClientCommand(clientCommand: Command, actRef: ActorRef[Engine.UpdateCommand]): Option[Engine.EngineCommand] = clientCommand match {
    case Command(CommandTypeHolder(CommandType.PAUSE), None) => Some(Engine.Pause())
    case Command(CommandTypeHolder(CommandType.RESUME), None) => Some(Engine.Resume())
    case Command(CommandTypeHolder(CommandType.MOVE), Some(data)) =>
      JSONConverter.fromJSON[MoveCommandTypeHolder](data) flatMap (parseClientMoveCommand(_, actRef))
    case _ => None
  }

  private def parseClientMoveCommand(clientMoveCommand: MoveCommandTypeHolder, actRef: ActorRef[UpdateCommand]): Option[Engine.EngineCommand] =
    clientMoveCommand match {
      case MoveCommandTypeHolder(MoveCommandType.UP) =>
        Some(Engine.ChangeDirectionReq(actRef, MoveDirection.UP))
      case MoveCommandTypeHolder(MoveCommandType.DOWN) =>
        Some(Engine.ChangeDirectionReq(actRef, MoveDirection.DOWN))
      case MoveCommandTypeHolder(MoveCommandType.LEFT) =>
        Some(Engine.ChangeDirectionReq(actRef, MoveDirection.LEFT))
      case MoveCommandTypeHolder(MoveCommandType.RIGHT) =>
        Some(Engine.ChangeDirectionReq(actRef, MoveDirection.RIGHT))
      case MoveCommandTypeHolder(MoveCommandType.NONE) =>
        Some(Engine.ChangeDirectionCur(actRef))
      case _ => None
    }
}
