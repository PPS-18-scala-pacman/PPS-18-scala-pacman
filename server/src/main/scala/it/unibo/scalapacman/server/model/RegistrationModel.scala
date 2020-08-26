package it.unibo.scalapacman.server.model

import akka.actor.typed.ActorRef
import it.unibo.scalapacman.server.core.Engine.UpdateCommand

import scala.collection.immutable

case class RegisteredParticipant(actor: ActorRef[UpdateCommand])

case class RegistrationModel(
                              blinky : Option[RegisteredParticipant] = None,
                              pinky  : Option[RegisteredParticipant] = None,
                              inky   : Option[RegisteredParticipant] = None,
                              clyde  : Option[RegisteredParticipant] = None,
                              pacman : Option[RegisteredParticipant] = None
                            ) {
  def toSeq: immutable.Seq[RegisteredParticipant] =
    (blinky :: pinky :: inky :: clyde :: pacman :: Nil) filter (_.isDefined) map (_.get)

  def isFull: Boolean = toSeq.size == 5
}
