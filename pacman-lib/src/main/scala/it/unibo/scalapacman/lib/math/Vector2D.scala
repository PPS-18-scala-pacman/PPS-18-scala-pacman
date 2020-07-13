package it.unibo.scalapacman.lib.math

case class Vector2D(x: Double, y: Double) {
  def +(vector2D: Vector2D): Vector2D =
    Vector2D(x + vector2D.x, y + vector2D.y)

  def *(number: Double): Vector2D = Vector2D(number * x, number * y)
}
