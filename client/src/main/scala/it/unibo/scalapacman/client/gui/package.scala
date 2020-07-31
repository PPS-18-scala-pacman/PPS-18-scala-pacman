package it.unibo.scalapacman.client

import java.awt.{Color, Font}

package object gui {

  val HEIGHT: Int = 1000
  val WIDTH: Int = 720

  val BUTTON_FONT_SIZE = 32
  val MAIN_TITLE_LABELS_FONT_SIZE = 86
  val TITLE_LABELS_FONT_SIZE = 56
  val MAIN_FONT_NAME = "Arial"

  val BACKGROUND_COLOR: Color = Color.BLACK
  val DEFAULT_TEXT_COLOR: Color = Color.WHITE
  val MAIN_TITLE_TEXT_COLOR: Color = Color.YELLOW

  def loadFont(fontPath: String): Font = Font.createFont(Font.TRUETYPE_FONT, getClass.getClassLoader.getResourceAsStream(fontPath))
}
