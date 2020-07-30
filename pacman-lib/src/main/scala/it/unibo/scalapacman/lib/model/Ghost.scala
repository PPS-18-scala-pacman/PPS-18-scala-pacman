package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.GhostType.GhostType

case class Ghost(
                  ghostType: GhostType,
                  override val position: Point2D,
                  override val speed: Double,
                  override val direction: Direction,
                  override val isDead: Boolean = false
                ) extends Character

object Ghost {
  val POINTS = 200

  def blinky(position: Point2D, speed: Double, direction: Direction): Ghost = Ghost(GhostType.BLINKY, position, speed, direction)

  def pinky(position: Point2D, speed: Double, direction: Direction): Ghost = Ghost(GhostType.PINKY, position, speed, direction)

  def inky(position: Point2D, speed: Double, direction: Direction): Ghost = Ghost(GhostType.INKY, position, speed, direction)

  def clyde(position: Point2D, speed: Double, direction: Direction): Ghost = Ghost(GhostType.CLYDE, position, speed, direction)
}
