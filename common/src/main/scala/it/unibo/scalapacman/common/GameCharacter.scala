package it.unibo.scalapacman.common

import it.unibo.scalapacman.lib.model.GhostType

object GameCharacter extends Enumeration {
  type GameCharacter = Value
  val PACMAN, BLINKY, PINKY, INKY, CLYDE = Value

  implicit def ghostTypeToGameCharacter(gt: GhostType.GhostType): GameCharacter = gt match {
    case GhostType.INKY   => INKY
    case GhostType.BLINKY => BLINKY
    case GhostType.CLYDE  => CLYDE
    case GhostType.PINKY  => PINKY
  }
}
