package it.unibo.scalapacman.lib.math

import scala.math.max

object Motion {

  /**
   * Calculate the end position
   * @param x starting position
   * @param vx speed
   * @param timeMs time in milliseconds
   * @return end position
   */
  def uniformLinearFor(x: Double, vx: Double, timeMs: Double): Double =
    MathUtility.round(x + vx * timeMs)

  /**
   * Calculate the time needed to reach the end position
   * @param x1 starting position
   * @param x2 end position
   * @param vx speed
   * @return time needed to reach end position
   * @throws java.lang.ArithmeticException when vx is zero and x2 - x1 is not zero
   */
  def uniformLinearUntil(x1: Double, x2: Double, vx: Double): Int = x2 - x1 match {
    case 0 => 0
    case _ if vx == 0 => throw new java.lang.ArithmeticException("/ by zero")
    case distance: Double => (distance / vx)
      .round
      .abs
      .toInt
  }

  /**
   *
   * @param position starting position
   * @param speed speed
   * @param timeMs time in milliseconds
   * @return end position
   */
  def uniformLinearFor(position: Point2D, speed: Vector2D, timeMs: Double): Point2D =
    Point2D(
      uniformLinearFor(position.x, speed.x, timeMs),
      uniformLinearFor(position.y, speed.y, timeMs)
    )

  /**
   *
   * @param fromPosition starting position
   * @param toPosition ending position
   * @param speed speed
   * @return time needed to reach end position
   */
  def uniformLinearUntil(fromPosition: Point2D, toPosition: Point2D, speed: Vector2D): Int =
    max(
      uniformLinearUntil(fromPosition.x, toPosition.x, speed.x),
      uniformLinearUntil(fromPosition.y, toPosition.y, speed.y)
    )
}
