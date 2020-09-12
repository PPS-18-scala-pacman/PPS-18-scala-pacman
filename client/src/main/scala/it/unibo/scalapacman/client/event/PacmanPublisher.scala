package it.unibo.scalapacman.client.event

import scala.collection.mutable

/**
 * Rappresenta l'entità che pubblica eventi di tipo PacmanEvent
 */
case class PacmanPublisher() extends mutable.Publisher[PacmanEvent] {
  def notifySubscribers(pe: PacmanEvent): Unit = publish(pe)
}
