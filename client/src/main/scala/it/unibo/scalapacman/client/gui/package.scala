package it.unibo.scalapacman.client

import java.awt.{Color, Font}

import javax.swing.text.{StyleConstants, StyleContext, StyledDocument}

package object gui {

  val FONT_PATH = "font/unifont/unifont.ttf"
  val UNIFONT: Font = loadFont

  val BUTTON_FONT_SIZE = 32
  val MAIN_TITLE_LABELS_FONT_SIZE = 86
  val TITLE_LABELS_FONT_SIZE = 56
  val MAIN_FONT_NAME = "Arial"

  val BACKGROUND_COLOR: Color = Color.BLACK
  val DEFAULT_TEXT_COLOR: Color = Color.WHITE
  val MAIN_TITLE_TEXT_COLOR: Color = Color.YELLOW

  /* TextPane constants */
  val PACMAN_SN = "pacman"
  val PACMAN_COLOR: Color = Color.YELLOW
  val PELLET_SN = "pellet"
  val PELLET_COLOR: Color = Color.WHITE

  /**
   * Aggiunge le informazioni sullo stile al documento
   * @param doc il documento di cui espandere lo stile
   */
  def addStylesToDocument(doc: StyledDocument): Unit = {
    // Stile di default, funge da root degli stili personalizzati che andremo ad aggiungere
    val default = StyleContext.getDefaultStyleContext.getStyle(StyleContext.DEFAULT_STYLE)

    var s = doc.addStyle(PACMAN_SN, default)
    StyleConstants.setForeground(s, PACMAN_COLOR)

    s = doc.addStyle(PELLET_SN, default)
    StyleConstants.setForeground(s, PELLET_COLOR)
  }

  private def loadFont: Font = {
    val stream = getClass.getClassLoader.getResourceAsStream(FONT_PATH)
    Font.createFont(Font.TRUETYPE_FONT, stream)
  }
}
