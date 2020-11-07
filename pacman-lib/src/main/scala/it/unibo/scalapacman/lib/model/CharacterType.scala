package it.unibo.scalapacman.lib.model

trait CharacterType

object PacmanType extends Enumeration {
  case class PacmanType() extends super.Val with CharacterType
  val PACMAN, MS_PACMAN, CAPMAN, RAPMAN = PacmanType()

  implicit def indexToPlayerTypeVal(index: Int): PacmanType = index match {
    case 0 => PACMAN
    case 1 => MS_PACMAN
    case 2 => CAPMAN
    case 3 => RAPMAN
  }
}

object GhostType extends Enumeration {
  case class GhostType() extends super.Val with CharacterType
  val BLINKY, PINKY, INKY, CLYDE = GhostType()
}
