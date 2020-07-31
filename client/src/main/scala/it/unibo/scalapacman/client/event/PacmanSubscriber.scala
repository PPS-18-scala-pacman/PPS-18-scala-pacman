package it.unibo.scalapacman.client.event

import scala.collection.mutable

case class PacmanSubscriber(handler: PacmanEvent => Unit) extends mutable.Subscriber[PacmanEvent, mutable.Publisher[PacmanEvent]] {
  override def notify(pub: mutable.Publisher[PacmanEvent], event: PacmanEvent): Unit = handler(event)
}
