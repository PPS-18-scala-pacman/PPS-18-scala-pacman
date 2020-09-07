package it.unibo.scalapacman.lib.math

object BlockGeography {
  val SIZE: Double = 1

  /**
   * The center relative to a block
   */
  val center: Point2D = Point2D(SIZE / 2, SIZE / 2)

  /**
   * Ritorna il punto al centro del lato ovest
   */
  val westGate: Point2D = Point2D(0, center.y)

  /**
   * Ritorna il punto al centro del lato est
   */
  val eastGate: Point2D = Point2D(SIZE, center.y)

  /**
   * Ritorna il punto al centro del lato nord
   */
  val northGate: Point2D = Point2D(center.x, 0)

  /**
   * Ritorna il punto al centro del lato sud
   */
  val southGate: Point2D = Point2D(center.x, SIZE)
}
