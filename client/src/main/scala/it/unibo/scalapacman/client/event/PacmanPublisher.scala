package it.unibo.scalapacman.client.event

import scala.collection.mutable

case class PacmanPublisher() extends mutable.Publisher[PacmanEvent] {
  def notifySubscribers(pe: PacmanEvent): Unit = publish(pe)
}
