package it.unibo.scalapacman.server.communication

import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.Message
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source, SourceQueueWithComplete}
import akka.stream.typed.scaladsl.ActorSource
import it.unibo.scalapacman.server.util.Settings

object StreamFactory {

  //FIXME
  def createActorWBSink(actor: ActorRef[Message]): Sink[Message, Any] = Flow[Message]
    .map(msg => actor ! msg)
    .to(Sink.ignore)
  /*
  Flow[Message].mapConcat {
      case tm: TextMessage =>
        actorRef ! TextMessage(tm.textStream)
        TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }
   */

  def createActorWBSource(): Source[Message, ActorRef[Message]] =
    ActorSource.actorRef[Message](
      completionMatcher = PartialFunction.empty,
      failureMatcher = PartialFunction.empty,
      bufferSize = Settings.bufferSizeWS,
      overflowStrategy = OverflowStrategy.dropHead)

  def createActorSimpleSink(handlerMsg: Message => Unit): Sink[Message, Any] = Flow[Message]
    .map(handlerMsg)
    .to(Sink.ignore)

  def createActorQueueSource(): Source[Message, SourceQueueWithComplete[Message]] =
    Source.queue[Message](Settings.bufferSizeWS, OverflowStrategy.dropHead)

}
