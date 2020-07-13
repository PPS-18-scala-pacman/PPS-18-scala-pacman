package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.math.{Point2D, TileGeography}
import it.unibo.scalapacman.lib.model.{Character, Direction, Map}

/**
 * Movement with Pacman effect
 */
object CircularMovement {

  def moveFor(map: Map, character: Character, timeMs: Double): Point2D = map.pacmanEffect(CharacterMovement.moveFor(character, timeMs))

  def moveUntil(map: Map, character: Character, endingPoint: Point2D): Double = character.direction match {
    case Direction.WEST if character.position.x < endingPoint.x => CharacterMovement.moveUntil(character, Point2D(endingPoint.x - map.width, endingPoint.y))
    case Direction.EAST if character.position.x > endingPoint.x => CharacterMovement.moveUntil(character, Point2D(endingPoint.x + map.width, endingPoint.y))
    case Direction.NORTH if character.position.y < endingPoint.y => CharacterMovement.moveUntil(character, Point2D(endingPoint.x, endingPoint.y - map.height))
    case Direction.SOUTH if character.position.y > endingPoint.y => CharacterMovement.moveUntil(character, Point2D(endingPoint.x, endingPoint.y + map.height))
    case _ => CharacterMovement.moveUntil(character, endingPoint)
  }

  implicit private class MapEnhanced(map: Map) {
    val width: Double = map.tiles.size * TileGeography.SIZE
    val height: Double = map.tiles.head.size * TileGeography.SIZE

    def pacmanEffect(point2D: Point2D): Point2D = Point2D(pacmanEffect(point2D.x, width), pacmanEffect(point2D.y, height))

    @scala.annotation.tailrec
    private def pacmanEffect(x: Double, max: Double): Double = x match {
      case x: Double if x > 0 => x % max
      case x: Double => pacmanEffect(x + max, max)
    }
  }

}
