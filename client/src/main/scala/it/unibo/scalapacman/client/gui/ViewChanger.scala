package it.unibo.scalapacman.client.gui

/**
 * Implementata da chi si occuperà di effettuare il cambio della schermata
 */
trait ViewChanger {
  /**
   * Effettua il cambio della schermata visualizzata
   *
   * @param view la nuova schermata da visualizzare
   */
  def changeView(view: View): Unit
}
