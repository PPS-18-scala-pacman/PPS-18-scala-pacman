package it.unibo.scalapacman.client.event

import scala.collection.mutable

class PacmanPublisher extends mutable.Publisher[PacmanEvent] {
  def notifySubscribers(pe: PacmanEvent): Unit = publish(pe)
}

object PacmanPublisher {
  def apply(): PacmanPublisher = new PacmanPublisher()
}
