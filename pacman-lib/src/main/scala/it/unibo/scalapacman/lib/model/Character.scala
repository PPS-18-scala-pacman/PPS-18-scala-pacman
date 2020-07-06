package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.math.Point2D

trait Character {
  val position: Point2D
  val speed: Double
}
