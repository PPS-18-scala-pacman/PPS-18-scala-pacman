package it.unibo.scalapacman.server.communication

import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.Message

object ConnectionProtocol {

  case class ConnectionAck()

  sealed trait ConnectionMsg

  case class ConnectionInit(ackTo: ActorRef[ConnectionAck]) extends ConnectionMsg
  case class ConnectionData(ackTo: ActorRef[ConnectionAck], msg: Message) extends ConnectionMsg
  case class ConnectionEnded() extends ConnectionMsg
  case class ConnectionFailed(ex: Throwable) extends ConnectionMsg

}
