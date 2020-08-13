package it.unibo.scalapacman.client.event

import org.scalamock.scalatest.MockFactory
import org.scalatest.wordspec.AnyWordSpec

class PacmanSubscriberTest extends AnyWordSpec with MockFactory {

  var _publisher: PacmanPublisher = _
  var _subscriber: PacmanSubscriber = _

  "PacmanSubscriber" should {
    "be notified when events are fired" in {
      val mockHanlder = stubFunction[PacmanEvent, Unit]

      _publisher = PacmanPublisher()
      _subscriber = PacmanSubscriber(mockHanlder)

      _publisher.subscribe(_subscriber)
      _publisher.notifySubscribers(TestEvent())

      mockHanlder.verify(TestEvent()).once()
    }
  }
}
