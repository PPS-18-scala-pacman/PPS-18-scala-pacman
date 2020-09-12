package it.unibo.scalapacman.client.gui

import java.awt.Color

/**
 * Rappresenta il colore da applicare ad un determinato stile.
 * Usato durante il disegno della mappa da mostrare all'utente.
 *
 * @param styleName nome dello stile a cui applicare il colore
 * @param foregroundColor il colore da utilizzare per quello stile
 */
case class ElementStyle(styleName: String, foregroundColor: Color)
