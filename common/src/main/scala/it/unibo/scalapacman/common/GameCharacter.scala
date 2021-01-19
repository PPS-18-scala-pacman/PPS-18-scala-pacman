package it.unibo.scalapacman.common

import it.unibo.scalapacman.lib.model.GhostType
import it.unibo.scalapacman.lib.model.PacmanType

object GameCharacter extends Enumeration {
  type GameCharacter = Value
  val PACMAN, MS_PACMAN, CAPMAN, RAPMAN, BLINKY, PINKY, INKY, CLYDE = Value

  implicit def ghostTypeToGameCharacter(gt: GhostType.GhostType): GameCharacter = gt match {
    case GhostType.INKY   => INKY
    case GhostType.BLINKY => BLINKY
    case GhostType.CLYDE  => CLYDE
    case GhostType.PINKY  => PINKY
  }

  implicit def playerTypeToGameCharacter(pt: PacmanType.PacmanType): GameCharacter = pt match {
    case PacmanType.PACMAN    => PACMAN
    case PacmanType.MS_PACMAN    => MS_PACMAN
    case PacmanType.CAPMAN  => CAPMAN
    case PacmanType.RAPMAN   => RAPMAN
  }
}

