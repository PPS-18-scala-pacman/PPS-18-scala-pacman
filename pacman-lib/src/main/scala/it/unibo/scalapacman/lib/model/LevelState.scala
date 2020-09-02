package it.unibo.scalapacman.lib.model

object LevelState extends Enumeration {
  type LevelState = Value
  val ONGOING, DEFEAT, VICTORY = Value
}
