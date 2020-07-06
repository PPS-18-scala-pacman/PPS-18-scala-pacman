package it.unibo.scalapacman.lib.math

import org.scalatest.wordspec.AnyWordSpec

class Point2DTest extends AnyWordSpec {
  val POINT1: Point2D = Point2D(0, 2)
  val POINT2: Point2D = Point2D(1, -1)
  val POINT_SUM: Point2D = Point2D(1, 1)

  "A Point2D" should {
    "be sum with another Point2D" in {
      assertResult(POINT_SUM)(POINT1 + POINT2)
    }
  }
}
