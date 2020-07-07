package it.unibo.scalapacman.lib.model

sealed trait GhostType

object GhostType {
  case object BLINKY extends GhostType
  case object PINKY extends GhostType
  case object INKY extends GhostType
  case object CLYDE extends GhostType
}
