package it.unibo.scalapacman.lib.model

object Direction extends Enumeration {
  type Direction = Value
  val WEST, EAST, NORTH, SOUTH, NORTHWEST, NORTHEAST, SOUTHWEST, SOUTHEAST = Value

  class DirectionVal(dir: Value) {
    def reverse: Direction = dir match {
      case EAST       => WEST
      case WEST       => EAST
      case NORTH      => SOUTH
      case SOUTH      => NORTH
      case NORTHWEST  => SOUTHEAST
      case NORTHEAST  => SOUTHWEST
      case SOUTHWEST  => NORTHEAST
      case SOUTHEAST  => NORTHWEST
    }

    def sharpTurnRight: Direction = dir match {
      case EAST       => SOUTH
      case WEST       => NORTH
      case NORTH      => EAST
      case SOUTH      => WEST
      case NORTHWEST  => NORTHEAST
      case NORTHEAST  => SOUTHEAST
      case SOUTHWEST  => NORTHWEST
      case SOUTHEAST  => SOUTHWEST
    }

    def sharpTurnLeft: Direction = dir.sharpTurnRight.reverse
  }
  import scala.language.implicitConversions
  implicit def valueToDirectionVal(x: Value): DirectionVal = new DirectionVal(x)

  def windRose: Set[Direction.Value] = Set(NORTH, SOUTH, EAST, WEST)
}
