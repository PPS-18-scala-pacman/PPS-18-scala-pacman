package it.unibo.scalapacman.client.gui

import java.awt.{Cursor, Font}

import javax.swing.{JButton, JLabel, JPanel}

trait Panel {
  def createLabel(text: String, width: Int, height: Int): JLabel

  def createButton(text: String, width: Int, height: Int): JButton
}

object Panel {
  class PanelImpl extends JPanel with Panel {

    override def createButton(text: String, width: Int, height: Int): JButton = new JButton(text) {
      setFocusPainted(false)
//      setBorderPainted(false)
      setContentAreaFilled(false)
      setSize(width, height)
      setCursor(new Cursor(Cursor.HAND_CURSOR))
    }

    override def createLabel(text: String, width: Int, height: Int): JLabel = new JLabel(text) {
      setSize(width, height)
    }
  }
}
