package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.Direction.Direction

trait Character extends GameObject {
  val position: Point2D
  val speed: Double
  val direction: Direction
  val isDead: Boolean
}
