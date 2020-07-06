package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.math.Point2D

case class Pacman(override val position: Point2D, override val speed: Double) extends Character