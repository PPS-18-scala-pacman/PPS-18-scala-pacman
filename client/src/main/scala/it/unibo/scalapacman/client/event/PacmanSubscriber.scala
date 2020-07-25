package it.unibo.scalapacman.client.event

import scala.collection.mutable

class PacmanSubscriber(handler: PacmanEvent => Unit) extends mutable.Subscriber[PacmanEvent, mutable.Publisher[PacmanEvent]] {
  override def notify(pub: mutable.Publisher[PacmanEvent], event: PacmanEvent): Unit = handler(event)
}

object PacmanSubscriber {
  def apply(handler: PacmanEvent => Unit): PacmanSubscriber = new PacmanSubscriber(handler)
}
