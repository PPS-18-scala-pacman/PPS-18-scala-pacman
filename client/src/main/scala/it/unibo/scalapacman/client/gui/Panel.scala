package it.unibo.scalapacman.client.gui

import java.awt.{Color, Cursor, Font}

import javax.swing.{JButton, JLabel, JPanel}

trait Panel {
  def createLabel(text: String, width: Int, height: Int): JLabel

  def createButton(text: String, width: Int, height: Int): JButton
}

object Panel {
  class PanelImpl extends JPanel with Panel {
    protected final val BUTTON_FONT_SIZE = 32
    protected final val FONT_NAME = "Arial"

    setBackground(Color.black)

    override def createButton(text: String, width: Int, height: Int): JButton = new JButton(text) {
      setFocusPainted(false)
//      setBorderPainted(false)
      setContentAreaFilled(false)
      setForeground(Color.white)
      setFont(new Font(FONT_NAME, Font.PLAIN, BUTTON_FONT_SIZE))
      setSize(width, height)
      setCursor(new Cursor(Cursor.HAND_CURSOR))
    }

    override def createLabel(text: String, width: Int, height: Int): JLabel = new JLabel(text) {
      setSize(width, height)
    }
  }
}
