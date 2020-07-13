package it.unibo.scalapacman.lib.math

import it.unibo.scalapacman.lib.math.Motion.{uniformLinearFor, uniformLinearUntil}
import org.scalatest.wordspec.AnyWordSpec

class MotionTest extends AnyWordSpec {

  case class TestData(startingPoint: Point2D, endingPoint: Point2D, timeMs: Int, speed: Vector2D)

  // scalastyle:off magic.number
  val successfulTests: Seq[TestData] =
    TestData(Point2D(0.0, 90.0), Point2D(100.0, 96.8), 10, Vector2D(10.0, 0.68)) ::
      TestData(Point2D(0.0, 96.8), Point2D(0.0, 96.8), 10, Vector2D(0.0, 0.0)) ::
      TestData(Point2D(0.0, 90.0), Point2D(70.0, 90.0), 7, Vector2D(10.0, 0.0)) ::
      TestData(Point2D(0.0, 90.0), Point2D(0.0, 90.0), 0, Vector2D(10.0, 0.68)) ::
      Nil

  val unreachableTest: TestData = TestData(Point2D(0.0, 90.0), Point2D(100.0, 96.8), 10, Vector2D(0.0, 0.0))
  // scalastyle:on magic.number

  val UNIDIMENSIONAL = "the space is unidimensional"
  val BIDIMENSIONAL = "the space is bidimensional"

  "A movement" should {
    "be calculated from a given time" when {

      UNIDIMENSIONAL in {
        for (test <- successfulTests) {
          assertResult(test.endingPoint.x)(uniformLinearFor(test.startingPoint.x, test.speed.x, test.timeMs))
          assertResult(test.endingPoint.y)(uniformLinearFor(test.startingPoint.y, test.speed.y, test.timeMs))
        }
      }
      BIDIMENSIONAL in {
        for (test <- successfulTests) {
          val endPosition = uniformLinearFor(test.startingPoint, test.speed, test.timeMs)
          assert(endPosition.x == test.endingPoint.x && endPosition.y == test.endingPoint.y)
        }
      }
    }
    "be calculated from a given destination" when {
      UNIDIMENSIONAL in {
        for (test <- successfulTests) {
          assert(uniformLinearUntil(test.startingPoint.x, test.endingPoint.x, test.speed.x) <= test.timeMs)
          assert(uniformLinearUntil(test.startingPoint.y, test.endingPoint.y, test.speed.y) <= test.timeMs)
        }
      }
      BIDIMENSIONAL in {
        for (test <- successfulTests)
          assert(uniformLinearUntil(test.startingPoint, test.endingPoint, test.speed) <= test.timeMs)
      }
    }
    "throw error when ending point is unreachable" when {
      UNIDIMENSIONAL in {
        assertThrows[ArithmeticException](uniformLinearUntil(unreachableTest.startingPoint.x, unreachableTest.endingPoint.x, unreachableTest.speed.x))
        assertThrows[ArithmeticException](uniformLinearUntil(unreachableTest.startingPoint.y, unreachableTest.endingPoint.y, unreachableTest.speed.y))
      }
      BIDIMENSIONAL in {
        assertThrows[ArithmeticException](uniformLinearUntil(unreachableTest.startingPoint, unreachableTest.endingPoint, unreachableTest.speed))
      }
    }
  }
}
