package it.unibo.scalapacman.lib.math

import scala.math.max

object Movement {

  /**
   * Calculate the end position
   * @param x starting position
   * @param vx speed
   * @param timeMs time in milliseconds
   * @return end position
   */
  def move(x: Double, vx: Double, timeMs: Int): Double =
    MathUtility.round(x + vx * timeMs)

  /**
   * Calculate the time needed to reach the end position
   * @param x1 starting position
   * @param x2 end position
   * @param vx speed
   * @return time needed to reach end position
   */
  def moveUntil(x1: Double, x2: Double, vx: Double): Int =
    ((x2 - x1) / vx)
    .round
    .toInt

  /**
   *
   * @param position starting position
   * @param speed speed
   * @param timeMs time in milliseconds
   * @return end position
   */
  def move(position: Point2D, speed: Vector2D, timeMs: Int): Point2D =
    Point2D(
      move(position.x, speed.x, timeMs),
      move(position.y, speed.y, timeMs)
    )

  /**
   *
   * @param fromPosition starting position
   * @param toPosition ending position
   * @param speed speed
   * @return time needed to reach end position
   */
  def moveUntil(fromPosition: Point2D, toPosition: Point2D, speed: Vector2D): Int =
    max(
      moveUntil(fromPosition.x, toPosition.x, speed.x),
      moveUntil(fromPosition.y, toPosition.y, speed.y)
    )
}
