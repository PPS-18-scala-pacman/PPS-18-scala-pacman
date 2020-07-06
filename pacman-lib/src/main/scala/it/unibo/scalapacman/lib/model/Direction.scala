package it.unibo.scalapacman.lib.model

sealed trait Direction

object Direction {
  case object WEST extends Direction
  case object EAST extends Direction
  case object NORTH extends Direction
  case object SOUTH extends Direction
  case object NORTHWEST extends Direction
  case object NORTHEAST extends Direction
  case object SOUTHWEST extends Direction
  case object SOUTHEAST extends Direction
}
