package it.unibo.scalapacman.client.input

sealed trait KeyStrokeIdentifier

/**
 * Identificatori utilizzati per la battuta dei tasti della tastiera
 */
object KeyStrokeIdentifier {

  /**
   * Identifica tasto Su premuto
   */
  case object UP_PRESSED extends KeyStrokeIdentifier
  /**
   * Identifica tasto Su rilasciato
   */
  case object UP_RELEASED extends KeyStrokeIdentifier
  /**
   * Identifica tasto Giu premuto
   */
  case object DOWN_PRESSED extends KeyStrokeIdentifier
  /**
   * Identifica tasto Giu rilasciato
   */
  case object DOWN_RELEASED extends KeyStrokeIdentifier
  /**
   * Identifica tasto Destro premuto
   */
  case object RIGHT_PRESSED extends KeyStrokeIdentifier
  /**
   * Identifica tasto Destro rilasciato
   */
  case object RIGHT_RELEASED extends KeyStrokeIdentifier
  /**
   * Identifica tasto Sinistra premuto
   */
  case object LEFT_PRESSED extends KeyStrokeIdentifier
  /**
   * Identifica tasto Sinistra rilasciato
   */
  case object LEFT_RELEASED extends KeyStrokeIdentifier
  /**
   * Identifica tasto Pausa/Ripristino
   */
  case object PAUSE_RESUME extends KeyStrokeIdentifier
}
