package it.unibo.scalapacman.lib.model

sealed trait GameEffect

object GameEffect {
  case object TURBO extends GameEffect
  case object GHOST_VULNERABLES extends GameEffect
  case object GHOST_REVERSE_DIRECTION extends GameEffect
}
