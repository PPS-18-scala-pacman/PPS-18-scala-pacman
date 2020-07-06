package it.unibo.scalapacman.lib.model

sealed trait Power

object Power {
  case object TURBO extends Power
  case object GHOST_VULNERABLES extends Power
  case object GHOST_REVERSE_DIRECTION extends Power
}
