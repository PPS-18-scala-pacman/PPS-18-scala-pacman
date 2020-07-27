package it.unibo.scalapacman.lib.model

object Direction extends Enumeration {
  type Direction = Value
  val WEST, EAST, NORTH, SOUTH, NORTHWEST, NORTHEAST, SOUTHWEST, SOUTHEAST = Value
}
