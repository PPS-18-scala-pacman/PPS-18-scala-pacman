package it.unibo.scalapacman.client.gui

import java.awt.{Color, Cursor, Font}
import javax.swing.{JButton, JLabel, JPanel}

trait Panel {
  def createLabel(text: String): JLabel
  def createLabel(text: String, width: Int, height: Int): JLabel
  def createMainTitleLabel(text: String): JLabel
  def createTitleLabel(text: String): JLabel

  def createButton(text: String): JButton
  def createButton(text: String, width: Int, height: Int): JButton
}

class PanelImpl extends JPanel with Panel {
  protected final val BUTTON_FONT_SIZE = 32
  private val LABEL_MAIN_TITLE_FONT_SIZE = 86
  private val LABEL_TITLE_FONT_SIZE = 56
  protected final val FONT_NAME = "Arial"

  setBackground(Color.black)

  override def createButton(text: String): JButton = new JButton(text) {
    setFocusPainted(false)
//    setBorderPainted(false)
    setContentAreaFilled(false)
    setForeground(Color.white)
    setFont(new Font(FONT_NAME, Font.PLAIN, BUTTON_FONT_SIZE))
    setCursor(new Cursor(Cursor.HAND_CURSOR))
  }

  override def createButton(text: String, width: Int, height: Int): JButton = {
    val button = createButton(text)
    button setSize(width, height)
    button
  }

  override def createLabel(text: String): JLabel = new JLabel(text) {
    setForeground(Color.white)
  }

  override def createLabel(text: String, width: Int, height: Int): JLabel = {
    val label = createLabel(text)
    label setSize(width, height)
    label
  }

  override def createMainTitleLabel(text: String): JLabel = {
    val label = createLabel(text)
    label setFont new Font(FONT_NAME, Font.BOLD, LABEL_MAIN_TITLE_FONT_SIZE)
    label
  }

  override def createTitleLabel(text: String): JLabel = {
    val label = createLabel(text)
    label setFont new Font(FONT_NAME, Font.BOLD, LABEL_TITLE_FONT_SIZE)
    label
  }
}
