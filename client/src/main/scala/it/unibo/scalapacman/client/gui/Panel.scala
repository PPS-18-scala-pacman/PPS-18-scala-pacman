package it.unibo.scalapacman.client.gui

import java.awt.{Cursor, Font}
import javax.swing.{JButton, JLabel, JPanel}

sealed trait Panel {
  def createLabel(text: String): JLabel
  def createLabel(text: String, width: Int, height: Int): JLabel
  def createMainTitleLabel(text: String): JLabel
  def createTitleLabel(text: String): JLabel

  def createButton(text: String): JButton
  def createButton(text: String, width: Int, height: Int): JButton
}

class PanelImpl extends JPanel with Panel {
  setBackground(BACKGROUND_COLOR)

  override def createButton(text: String): JButton = new JButton(text) {
    setFocusPainted(false)
//    setBorderPainted(false)
    setContentAreaFilled(false)
    setForeground(DEFAULT_TEXT_COLOR)
    setFont(new Font(MAIN_FONT_NAME, Font.PLAIN, BUTTON_FONT_SIZE))
    setCursor(new Cursor(Cursor.HAND_CURSOR))
  }

  override def createButton(text: String, width: Int, height: Int): JButton = {
    val button = createButton(text)
    button setSize(width, height)
    button
  }

  override def createLabel(text: String): JLabel = new JLabel(text) {
    setForeground(DEFAULT_TEXT_COLOR)
  }

  override def createLabel(text: String, width: Int, height: Int): JLabel = {
    val label = createLabel(text)
    label setSize(width, height)
    label
  }

  override def createMainTitleLabel(text: String): JLabel = {
    val label = createLabel(text)
    label setFont new Font(MAIN_FONT_NAME, Font.BOLD, MAIN_TITLE_LABELS_FONT_SIZE)
    label
  }

  override def createTitleLabel(text: String): JLabel = {
    val label = createLabel(text)
    label setFont new Font(MAIN_FONT_NAME, Font.BOLD, TITLE_LABELS_FONT_SIZE)
    label
  }
}
