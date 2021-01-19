package it.unibo.scalapacman.client.input

/**
 * Rappresenza la configurazione della tastiera
 * @param up codice per comando Su
 * @param down codice per comando Giu
 * @param right codice per comando Destra
 * @param left codice per comando Sinistra
 * @param pause codice per comando Pausa/Ripresa gioco
 */
case class KeyMap(up: Int, down: Int, right: Int, left: Int, pause: Int)
