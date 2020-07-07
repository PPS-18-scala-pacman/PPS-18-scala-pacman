package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.math.Point2D

case class Ghost(ghostType: GhostType, override val position: Point2D, override val speed: Double)
  extends Character
    with Eatable {
  val points: Int = Ghost.POINTS
}

object Ghost {
  val POINTS = 200

  def blinky(position: Point2D, speed: Double): Ghost = Ghost(GhostType.BLINKY, position, speed)

  def pinky(position: Point2D, speed: Double): Ghost = Ghost(GhostType.PINKY, position, speed)

  def inky(position: Point2D, speed: Double): Ghost = Ghost(GhostType.INKY, position, speed)

  def clyde(position: Point2D, speed: Double): Ghost = Ghost(GhostType.CLYDE, position, speed)
}
