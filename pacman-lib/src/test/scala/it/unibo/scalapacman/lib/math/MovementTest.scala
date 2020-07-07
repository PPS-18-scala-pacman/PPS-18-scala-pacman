package it.unibo.scalapacman.lib.math

import it.unibo.scalapacman.lib.math.Movement.{ move, moveUntil }
import org.scalatest.wordspec.AnyWordSpec

class MovementTest extends AnyWordSpec {
  val STARTING_POINT: Point2D = Point2D(0.0, 90.0)
  val ENDING_POINT: Point2D = Point2D(100.0, 96.8)
  val TIME_MS = 10
  val SPEED: Vector2D = Vector2D(10.0, 0.68)

  "A movement" can {
    "be calculated from a given time" when {

      "the space is unidimensional" in {
        assertResult(ENDING_POINT.x)(move(STARTING_POINT.x, SPEED.x, TIME_MS))
        assertResult(ENDING_POINT.y)(move(STARTING_POINT.y, SPEED.y, TIME_MS))
      }
      "the space is bidimensional" in {
        val endPosition = move(STARTING_POINT, SPEED, TIME_MS)
        assert(endPosition.x == ENDING_POINT.x && endPosition.y == ENDING_POINT.y)
      }
    }
    "be calculated from a given destination" when {
      "the space is unidimensional" in {
        assertResult(TIME_MS)(moveUntil(STARTING_POINT.x, ENDING_POINT.x, SPEED.x))
        assertResult(TIME_MS)(moveUntil(STARTING_POINT.y, ENDING_POINT.y, SPEED.y))
      }
      "the space is bidimensional" in {
        assertResult(TIME_MS)(moveUntil(STARTING_POINT, ENDING_POINT, SPEED))
      }
    }
  }
}
