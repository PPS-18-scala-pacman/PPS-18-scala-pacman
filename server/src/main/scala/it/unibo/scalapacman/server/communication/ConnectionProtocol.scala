package it.unibo.scalapacman.server.communication

import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.Message

/**
 * Classe che contiene i messaggi definiti dal protocollo di connesione tramite websocket
 */
object ConnectionProtocol {

  sealed trait Ack
  case class ConnectionAck() extends Ack

  sealed trait ConnectionMsg

  case class ConnectionInit(ackTo: ActorRef[Ack]) extends ConnectionMsg
  case class ConnectionData(ackTo: ActorRef[Ack], msg: Message) extends ConnectionMsg
  case class ConnectionEnded() extends ConnectionMsg
  case class ConnectionFailed(ex: Throwable) extends ConnectionMsg

}
