package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.Direction.Direction

case class Pacman(override val position: Point2D, override val speed: Double, override val direction: Direction) extends Character
