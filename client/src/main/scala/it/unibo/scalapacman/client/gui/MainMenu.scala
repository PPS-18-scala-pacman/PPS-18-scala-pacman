package it.unibo.scalapacman.client.gui

import java.awt.{Color, Font}

import it.unibo.scalapacman.client.gui.Panel.PanelImpl
import javax.swing.{JButton, JLabel}

class MainMenu extends PanelImpl {
  private val LABEL_X = 180
  private val LABEL_Y = 105
  private val LABEL_HEIGHT = 300
  private val LABEL_WIDTH = 400
  private val LABEL_FONT_SIZE = 86
  private val BUTTON_X = 270
  private val BUTTON_Y = 505
  private val BUTTON_Y_OFFSET = 100
  private val BUTTON_HEIGHT = 75
  private val BUTTON_WIDTH = 150

  private val title: JLabel = createLabel("<html><div style='text-align: center'>Scala<br>Pacman<div></html>", LABEL_WIDTH, LABEL_HEIGHT)

  title setLocation (LABEL_X, LABEL_Y)
  title setForeground Color.yellow
  title setFont new Font(FONT_NAME, Font.BOLD, LABEL_FONT_SIZE)

  private val playButton: JButton = createButton("Gioca", BUTTON_WIDTH, BUTTON_HEIGHT)

  playButton setLocation (BUTTON_X, BUTTON_Y)

  private val exitButton: JButton = createButton("Esci", BUTTON_WIDTH, BUTTON_HEIGHT)

  exitButton setLocation (BUTTON_X, BUTTON_Y + BUTTON_Y_OFFSET)
  exitButton addActionListener (_ => System exit 0)

  //noinspection ScalaStyle
  setLayout(null)
  add(title)
  add(playButton)
  add(exitButton)
}
