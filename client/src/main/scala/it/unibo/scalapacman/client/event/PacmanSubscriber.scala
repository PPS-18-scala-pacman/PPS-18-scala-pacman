package it.unibo.scalapacman.client.event

import scala.collection.mutable

/**
 * Rappresenta l'entità che sta in ascolto di eventi di tipo PacmanEvent
 * @param handler la funzione gestisce gli eventi
 */
case class PacmanSubscriber(handler: PacmanEvent => Unit) extends mutable.Subscriber[PacmanEvent, mutable.Publisher[PacmanEvent]] {
  override def notify(pub: mutable.Publisher[PacmanEvent], event: PacmanEvent): Unit = handler(event)
}
