package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.engine.CharacterMovement.{moveFor, moveUntil}
import it.unibo.scalapacman.lib.math.{Point2D, Vector2D}
import it.unibo.scalapacman.lib.model.{Pacman, Direction}
import org.scalatest.wordspec.AnyWordSpec

class CharacterMovementTest extends AnyWordSpec {

  val STARTING_POINT: Point2D = Point2D(0.0, 90.0)
  val TIME_MS = 10.0
  val SPEED = 10.0
  val DISTANCE: Double = SPEED * TIME_MS

  val PACMAN_WEST: Pacman = Pacman(STARTING_POINT, SPEED, Direction.WEST)
  val ENDING_POINT_WEST: Point2D = Point2D(STARTING_POINT.x - DISTANCE, STARTING_POINT.y)
  val PACMAN_EAST: Pacman = Pacman(STARTING_POINT, SPEED, Direction.EAST)
  val ENDING_POINT_EAST: Point2D = Point2D(STARTING_POINT.x + DISTANCE, STARTING_POINT.y)
  val PACMAN_NORTH: Pacman = Pacman(STARTING_POINT, SPEED, Direction.NORTH)
  val ENDING_POINT_NORTH: Point2D = Point2D(STARTING_POINT.x, STARTING_POINT.y - DISTANCE)
  val PACMAN_SOUTH: Pacman = Pacman(STARTING_POINT, SPEED, Direction.SOUTH)
  val ENDING_POINT_SOUTH: Point2D = Point2D(STARTING_POINT.x, STARTING_POINT.y + DISTANCE)

  "A movement" can {
    "be calculated from a given time" when {
      "used on the Pacman character" in {
        val endingPoint2 = moveFor(PACMAN_WEST, TIME_MS)
        assert(endingPoint2.x == ENDING_POINT_WEST.x && endingPoint2.y == ENDING_POINT_WEST.y)

        val endingPoint1 = moveFor(PACMAN_EAST, TIME_MS)
        assert(endingPoint1.x == ENDING_POINT_EAST.x && endingPoint1.y == ENDING_POINT_EAST.y)

        val endingPoint3 = moveFor(PACMAN_NORTH, TIME_MS)
        assert(endingPoint3.x == ENDING_POINT_NORTH.x && endingPoint3.y == ENDING_POINT_NORTH.y)

        val endingPoint4 = moveFor(PACMAN_SOUTH, TIME_MS)
        assert(endingPoint4.x == ENDING_POINT_SOUTH.x && endingPoint4.y == ENDING_POINT_SOUTH.y)
      }
      "used on the Ghost character" in pending
    }
    "be calculated from a given destination" when {
      "used on the Pacman character" in {
        val timeMs1 = moveUntil(PACMAN_WEST, ENDING_POINT_WEST)
        assertResult(TIME_MS)(timeMs1)

        val timeMs2 = moveUntil(PACMAN_EAST, ENDING_POINT_EAST)
        assertResult(TIME_MS)(timeMs2)

        val timeMs3 = moveUntil(PACMAN_NORTH, ENDING_POINT_NORTH)
        assertResult(TIME_MS)(timeMs3)

        val timeMs4 = moveUntil(PACMAN_SOUTH, ENDING_POINT_SOUTH)
        assertResult(TIME_MS)(timeMs4)
      }
      "used on the Ghost character" in pending
    }
  }

}
