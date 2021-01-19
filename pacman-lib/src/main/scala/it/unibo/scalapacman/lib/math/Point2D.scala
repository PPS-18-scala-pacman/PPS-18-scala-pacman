package it.unibo.scalapacman.lib.math

import scala.math.{ pow, sqrt }

case class Point2D(x: Double, y: Double) {
  def +(point2D: Point2D): Point2D = Point2D(x + point2D.x, y + point2D.y)
  def distance(point2D: Point2D): Double = sqrt(pow(x - point2D.x, 2) + pow(y - point2D.y, 2))
}
