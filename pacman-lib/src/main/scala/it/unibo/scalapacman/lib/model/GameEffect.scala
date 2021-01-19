package it.unibo.scalapacman.lib.model

sealed trait GameEffect

object GameEffect {

  /**
   * Effetto dell'energizer su Pacman, ne aumenta la velocità.
   */
  case object TURBO extends GameEffect

  /**
   * Effetto dell'energizer sui fantasmi: finchè sono vulnerabili
   * possono essere mangiati da Pacman.
   */
  case object GHOST_VULNERABLES extends GameEffect

  /**
   * Effetto dell'energizer sui fantasmi: quando viene mangiato i fantasmi
   * invertono la propria direzione
   */
  case object GHOST_REVERSE_DIRECTION extends GameEffect
}
