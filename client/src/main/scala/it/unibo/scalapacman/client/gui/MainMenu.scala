package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, Color, Component, Dimension, Font}

import javax.swing.{Box, BoxLayout, JButton, JLabel, JSplitPane, SwingConstants}

class MainMenu extends PanelImpl {
  private val LABEL_FONT_SIZE = 86
  private val BUTTONS_HORIZONTAL_SPACE = 0
  private val BUTTONS_VERTICAL_SPACE = 50
  private val BUTTONS_TOP_SPACE = 50

  private val titleLabel: JLabel = createLabel("<html><div style='text-align: center'>Scala<br>Pacman<div></html>")
  private val playButton: JButton = createButton("Gioca")
  private val optionsButton: JButton = createButton("Opzioni")
  private val statsButton: JButton = createButton("Statistiche")
  private val exitButton: JButton = createButton("Esci")

  titleLabel setForeground Color.yellow
  titleLabel setFont new Font(FONT_NAME, Font.BOLD, LABEL_FONT_SIZE)
  titleLabel setHorizontalAlignment SwingConstants.CENTER

  playButton setAlignmentX Component.CENTER_ALIGNMENT
  optionsButton setAlignmentX Component.CENTER_ALIGNMENT
  statsButton setAlignmentX Component.CENTER_ALIGNMENT
  exitButton setAlignmentX Component.CENTER_ALIGNMENT

  exitButton addActionListener (_ => System exit 0)

  private val splitPane = new JSplitPane
  private val titlePanel = new PanelImpl
  private val buttonsPanel = new PanelImpl

  splitPane setOrientation JSplitPane.VERTICAL_SPLIT
  splitPane setDividerLocation 300
  splitPane setDividerSize 0
  splitPane setEnabled false
  splitPane setTopComponent titlePanel
  splitPane setBottomComponent buttonsPanel

  buttonsPanel setLayout new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS)
  titlePanel setLayout new BorderLayout

  buttonsPanel add createRigidArea(BUTTONS_HORIZONTAL_SPACE, BUTTONS_TOP_SPACE)
  buttonsPanel add playButton
  buttonsPanel add createRigidArea(BUTTONS_HORIZONTAL_SPACE, BUTTONS_VERTICAL_SPACE)
  buttonsPanel add optionsButton
  buttonsPanel add createRigidArea(BUTTONS_HORIZONTAL_SPACE, BUTTONS_VERTICAL_SPACE)
  buttonsPanel add statsButton
  buttonsPanel add createRigidArea(BUTTONS_HORIZONTAL_SPACE, BUTTONS_VERTICAL_SPACE)
  buttonsPanel add exitButton

  titlePanel add (titleLabel, BorderLayout.CENTER)

  setLayout(new BorderLayout)
  add(splitPane)

  private def createRigidArea(horizontalArea: Int, verticalArea: Int): Component = Box.createRigidArea(new Dimension(horizontalArea, verticalArea))
}
