package it.unibo.scalapacman.server.util

import akka.actor.typed.ActorRef
import it.unibo.scalapacman.common.GameCharacter
import it.unibo.scalapacman.common.GameCharacter.{BLINKY, CLYDE, GameCharacter, INKY, PACMAN, PINKY}
import it.unibo.scalapacman.server.core.Engine.UpdateCommand
import it.unibo.scalapacman.server.model.{EngineModel, Players, RegistrationModel}

object RecoveryHelper {

  def replacePartecipant(regModel: RegistrationModel,
                          engModel: EngineModel,
                          charType: GameCharacter,
                          actor: ActorRef[UpdateCommand]): (RegistrationModel, EngineModel) =
    (RegistrationHelper.registerPartecipant(regModel, charType, actor),
      engModel.copy(players = updatePlayers(engModel.players, charType, actor)))

  private def updatePlayers(pl: Players, charType: GameCharacter, actor: ActorRef[UpdateCommand]): Players =
    charType match {
      case PACMAN => pl.copy(pacman = pl.pacman.copy(actRef = actor))
      case BLINKY => pl.copy(blinky = pl.blinky.copy(actRef = actor))
      case INKY   => pl.copy(inky   = pl.inky.copy(actRef = actor))
      case PINKY  => pl.copy(pinky  = pl.pinky.copy(actRef = actor))
      case CLYDE  => pl.copy(clyde  = pl.clyde.copy(actRef = actor))
    }

  def createRecoveryModel(charFailed: GameCharacter, engModel: EngineModel): RegistrationModel =
    GameCharacter.values.filter(_ != charFailed).foldRight(RegistrationModel()) {
      (charType, regModel) =>
        RegistrationHelper.registerPartecipant(regModel, charType, engModel.players.get(charType).actRef)
    }

}
