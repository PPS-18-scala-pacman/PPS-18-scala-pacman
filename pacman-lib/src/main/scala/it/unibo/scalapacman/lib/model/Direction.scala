package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.model.Map.MapIndexes
import it.unibo.scalapacman.lib.engine.GameHelpers.{MapHelper, CharacterHelper}

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

  def byPath(path: (MapIndexes, MapIndexes)): Direction = path match {
    case ((x, _), (x1, _)) if x < x1 => Direction.EAST
    case ((x, _), (x1, _)) if x > x1 => Direction.WEST
    case ((_, y), (_, y1)) if y < y1 => Direction.SOUTH
    case ((_, y), (_, y1)) if y > y1 => Direction.NORTH
  }

  def byCrossTile(path: (MapIndexes, MapIndexes), char: Character)(implicit map: Map): Option[Direction] =
    windRose.find(
      dir => map.nextTile(path._1, dir).walkable(char) && char.nextCrossTile(path._1, dir).contains(path._2)
    )
}
