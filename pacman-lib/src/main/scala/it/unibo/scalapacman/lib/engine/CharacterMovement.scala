package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.math.{Motion, Point2D, Vector2D}
import it.unibo.scalapacman.lib.model.Direction.{EAST, NORTH, NORTHEAST, NORTHWEST, SOUTH, SOUTHEAST, SOUTHWEST, WEST}
import it.unibo.scalapacman.lib.model.Character
import it.unibo.scalapacman.lib.model.Direction.Direction

object CharacterMovement {
  def moveFor(character: Character, timeMs: Double): Point2D =
    Motion.uniformLinearFor(character.position, character.speedVector, timeMs)

  def moveUntil(character: Character, endingPoint: Point2D): Double =
    Motion.uniformLinearUntil(character.position, endingPoint, character.speedVector)

  implicit private class CharacterEnhanced(character: Character) {
    def speedVector: Vector2D = character.direction * character.speed
  }

  /**
   * Versore
   */
  implicit def unitVector(direction: Direction): Vector2D = direction match {
    case NORTH => Vector2D(0, -1)
    case SOUTH => Vector2D(0, 1)
    case WEST => Vector2D(-1, 0)
    case EAST => Vector2D(1, 0)
    case NORTHWEST => unitVector(NORTH) + unitVector(WEST)
    case NORTHEAST => unitVector(NORTH) + unitVector(EAST)
    case SOUTHWEST => unitVector(SOUTH) + unitVector(WEST)
    case SOUTHEAST => unitVector(SOUTH) + unitVector(EAST)
  }

}
