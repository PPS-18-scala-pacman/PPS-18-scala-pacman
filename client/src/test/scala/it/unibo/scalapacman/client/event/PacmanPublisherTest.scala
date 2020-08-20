package it.unibo.scalapacman.client.event

import org.scalamock.scalatest.MockFactory
import org.scalatest.wordspec.AnyWordSpecLike

import scala.collection.mutable

class PacmanPublisherTest
  extends AnyWordSpecLike
  with MockFactory {

  var _publisher: PacmanPublisher = _

  "PacmanPublisher" should {
    "notify Subscribers who subscribed" in {
      class MockableSubscriber extends mutable.Subscriber[PacmanEvent, mutable.Publisher[PacmanEvent]] {
        override def notify(pub: mutable.Publisher[PacmanEvent], event: PacmanEvent): Unit = Unit
      }

      _publisher = PacmanPublisher()

      val mockableSubscriber = stub[MockableSubscriber]

//      https://github.com/paulbutcher/ScalaMock/issues/220 spiega perché serva ScalaMock 4.4.0 per fare stub di notify
      (mockableSubscriber.notify (_: mutable.Publisher[PacmanEvent], _: PacmanEvent)).when(_publisher, TestEvent())

      _publisher.subscribe(mockableSubscriber)

      _publisher.notifySubscribers(TestEvent())

      (mockableSubscriber.notify (_: mutable.Publisher[PacmanEvent], _: PacmanEvent)).verify(_publisher, TestEvent()).once()
    }
  }

}
