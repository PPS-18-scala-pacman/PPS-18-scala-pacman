package it.unibo.scalapacman.lib.math


object BlockGeography {
  val SIZE: Double = 1

  /**
   * The center relative to a block
   */
  val center: Point2D = Point2D(SIZE / 2, SIZE / 2)

  val westGate: Point2D = Point2D(0, center.y)
  val eastGate: Point2D = Point2D(SIZE, center.y)
  val northGate: Point2D = Point2D(center.x, 0)
  val southGate: Point2D = Point2D(center.x, SIZE)
}
