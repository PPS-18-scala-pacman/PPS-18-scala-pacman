package it.unibo.scalapacman.client.event

import grizzled.slf4j.Logging
import org.scalamock.scalatest.MockFactory
import org.scalatest.wordspec.AnyWordSpecLike

import scala.collection.mutable

class PacmanPublisherTest
  extends AnyWordSpecLike
  with MockFactory
  with Logging {

  var publisher: PacmanPublisher = _

//  "PacmanPublisher" should {
//    "notify Subscribers who subscribed" in {
//      case class TestEvent(count: Int) extends PacmanEvent
//      var test: Int = 0
//      val _count: Int = 999
//      publisher = PacmanPublisher()
//
//      debug(s"Test è $test")
//      val subscriber = PacmanSubscriber({
//        case TestEvent(count) => test += count
//        case _ => test += 1
//      })
//
//      publisher.subscribe(subscriber)
//      publisher.notifySubscribers(TestEvent(_count))
//
//      debug(s"Test è $test")
//      assertResult(_count)(test)
//
//    }
//  }

  "PacmanPublisher" should {
    "notify Subscribers who subscribed" in {
      case class TestEvent() extends PacmanEvent

      class MockableSubscriber extends mutable.Subscriber[PacmanEvent, mutable.Publisher[PacmanEvent]] {
        override def notify(pub: mutable.Publisher[PacmanEvent], event: PacmanEvent): Unit = Unit
      }

      publisher = PacmanPublisher()

      val mockableSubscriber = stub[MockableSubscriber]

//      https://github.com/paulbutcher/ScalaMock/issues/220 spiega perché serva ScalaMock 4.4.0 per fare stub di notify
      (mockableSubscriber.notify (_: mutable.Publisher[PacmanEvent], _: PacmanEvent)).when(publisher, TestEvent())

      publisher.subscribe(mockableSubscriber)

      publisher.notifySubscribers(TestEvent())

      (mockableSubscriber.notify (_: mutable.Publisher[PacmanEvent], _: PacmanEvent)).verify(publisher, TestEvent()).once()
    }
  }

}
