package it.unibo.scalapacman.server.util

import akka.actor.typed.ActorRef
import it.unibo.scalapacman.common.GameCharacter.{BLINKY, CLYDE, GameCharacter, INKY, PACMAN, PINKY}
import it.unibo.scalapacman.server.core.Engine.UpdateCommand
import it.unibo.scalapacman.server.model.{RegisteredParticipant, RegistrationModel}

object RegistrationHelper {

  def registerPartecipant(model: RegistrationModel,
                          charType: GameCharacter,
                          actor: ActorRef[UpdateCommand]): RegistrationModel = updatePartecipant(model, charType, Some(actor))

  def unRegisterPartecipant(model: RegistrationModel,
                          charType: GameCharacter): RegistrationModel = updatePartecipant(model, charType, None)

  private def updatePartecipant(model: RegistrationModel,
                                charType: GameCharacter,
                                actor: Option[ActorRef[UpdateCommand]]): RegistrationModel =
    charType match {
      case PACMAN => model.copy(pacman = actor.map(RegisteredParticipant))
      case BLINKY => model.copy(blinky = actor.map(RegisteredParticipant))
      case INKY   => model.copy(inky   = actor.map(RegisteredParticipant))
      case PINKY  => model.copy(pinky  = actor.map(RegisteredParticipant))
      case CLYDE  => model.copy(clyde  = actor.map(RegisteredParticipant))
    }
}
