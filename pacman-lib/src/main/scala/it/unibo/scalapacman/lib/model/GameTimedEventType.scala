package it.unibo.scalapacman.lib.model

object GameTimedEventType extends Enumeration {
  type GameTimedEventType = Value
  val GHOST_RESTART, FRUIT_SPAWN, FRUIT_STOP, ENERGIZER_STOP = Value
}
