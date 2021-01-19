package it.unibo.scalapacman.server.communication

import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.Message
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import it.unibo.scalapacman.server.communication.ConnectionProtocol.{Ack, ConnectionAck, ConnectionData,
  ConnectionEnded, ConnectionFailed, ConnectionInit, ConnectionMsg}
import it.unibo.scalapacman.server.config.Settings

/**
 * Contiene le funzioni di utility per la crezione di Sink e Source per costrunzione Flow di connessione
 */
object StreamFactory {

  def createActorWBSink(actor: ActorRef[ConnectionMsg]): Sink[Message, NotUsed] =
    ActorSink.actorRefWithBackpressure[Message, ConnectionMsg, Ack](
      ref = actor,
      onInitMessage = ConnectionInit.apply,
      messageAdapter = ConnectionData.apply,
      ackMessage = ConnectionAck(),
      onCompleteMessage = ConnectionEnded(),
      onFailureMessage = ConnectionFailed.apply
    )

  def createActorWBSource(): Source[Message, ActorRef[Message]] =
    ActorSource.actorRef[Message](
      completionMatcher = PartialFunction.empty,
      failureMatcher = PartialFunction.empty,
      bufferSize = Settings.bufferSizeWS,
      overflowStrategy = OverflowStrategy.dropHead)
}
