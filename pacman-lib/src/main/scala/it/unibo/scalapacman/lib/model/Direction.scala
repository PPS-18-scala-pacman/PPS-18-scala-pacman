package it.unibo.scalapacman.lib.model

object Direction extends Enumeration {
  type Direction = Value
  val WEST, EAST, NORTH, SOUTH, NORTHWEST, NORTHEAST, SOUTHWEST, SOUTHEAST = Value

  //TODO check
  def reverse(dir: Direction): Direction = dir match {
    case EAST       => WEST
    case WEST       => EAST
    case NORTH      => SOUTH
    case SOUTH      => NORTH
    case NORTHWEST  => SOUTHEAST
    case NORTHEAST  => SOUTHWEST
    case SOUTHWEST  => NORTHEAST
    case SOUTHEAST  => NORTHWEST
  }
}
