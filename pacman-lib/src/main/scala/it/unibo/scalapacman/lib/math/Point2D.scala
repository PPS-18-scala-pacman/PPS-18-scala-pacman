package it.unibo.scalapacman.lib.math

case class Point2D(x: Double, y: Double) {
  def +(point2D: Point2D): Point2D = Point2D(x + point2D.x, y + point2D.y)
}
