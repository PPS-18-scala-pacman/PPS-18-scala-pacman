package it.unibo.scalapacman.client.gui

import java.awt.Color

import it.unibo.scalapacman.lib.model.GameState

/**
 * Rappresenta il colore da applicare ad un determinato stile.
 * Usato durante il disegno della mappa da mostrare all'utente.
 *
 * @param foregroundColor il colore da utilizzare per quello stile
 */
case class ElementStyle(foregroundColor: Either[Color, GameState => Color])

object ResolvedElementStyle {
  def apply(style: ElementStyle, gameState: GameState): ResolvedElementStyle =
    new ResolvedElementStyle(
      foregroundColor = style.foregroundColor.map(_(gameState)).merge
    )
}

case class ResolvedElementStyle(foregroundColor: Color)
