package it.unibo.scalapacman.client.gui

sealed trait View { def name: String }

/**
 * Identificativi dei componenti della View utilizzati per gestire il cambio schermata
 */
object View {
  /** Identifica la schermata iniziale, quella del menù */
  case object MENU extends View { val name = "menu" }
  /** Identifica la schermata per la scelta della modalità di gioco */
  case object SETUP extends View { val name = "setup" }
  /** Identifica la schermata di gioco, dove l'utente effettivamente può giocare a Pacman */
  case object PLAY extends View { val name = "play" }
  /** Identifica la schermata di configurazione tasti */
  case object OPTIONS extends View { val name = "options" }
  /** Identifica la schermata delle statistiche */
  case object STATS extends View { val name = "stats" }
}
