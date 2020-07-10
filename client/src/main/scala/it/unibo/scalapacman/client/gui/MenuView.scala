package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, Component, Dimension}

import it.unibo.scalapacman.client.utility.Action.{CHANGE_VIEW, EXIT_APP}
import it.unibo.scalapacman.client.controller.GameController
import it.unibo.scalapacman.client.utility.View.{OPTIONS, PLAY, STATS}
import javax.swing.{Box, BoxLayout, JButton, JLabel, JSplitPane, SwingConstants}

class MenuView extends PanelImpl {
  private val MAIN_TITLE_LABEL: String = "<html><div style='text-align: center'>Scala<br>Pacman<div></html>"
  private val PLAY_VIEW_BUTTON_LABEL: String = "Gioca"
  private val OPTIONS_VIEW_BUTTON_LABEL: String = "Opzioni"
  private val STATS_VIEW_BUTTON_LABEL: String = "Statistiche"
  private val EXIT_BUTTON_LABEL: String = "Esci"
  private val BUTTONS_HORIZONTAL_SPACE: Int = 0
  private val BUTTONS_VERTICAL_SPACE: Int = 50
  private val BUTTONS_TOP_SPACE: Int = 50

  private val titleLabel: JLabel = createMainTitleLabel(MAIN_TITLE_LABEL)
  private val playButton: JButton = createButton(PLAY_VIEW_BUTTON_LABEL)
  private val optionsButton: JButton = createButton(OPTIONS_VIEW_BUTTON_LABEL)
  private val statsButton: JButton = createButton(STATS_VIEW_BUTTON_LABEL)
  private val exitButton: JButton = createButton(EXIT_BUTTON_LABEL)

  titleLabel setForeground MAIN_TITLE_TEXT_COLOR
  titleLabel setHorizontalAlignment SwingConstants.CENTER

  playButton setAlignmentX Component.CENTER_ALIGNMENT
  optionsButton setAlignmentX Component.CENTER_ALIGNMENT
  statsButton setAlignmentX Component.CENTER_ALIGNMENT
  exitButton setAlignmentX Component.CENTER_ALIGNMENT

  playButton addActionListener (_ => GameController.handleAction(CHANGE_VIEW, Some(PLAY)))
  optionsButton addActionListener (_ => GameController.handleAction(CHANGE_VIEW, Some(OPTIONS)))
  statsButton addActionListener (_ => GameController.handleAction(CHANGE_VIEW, Some(STATS)))
  exitButton addActionListener (_ => GameController.handleAction(EXIT_APP, None))

  private val splitPane: JSplitPane = new JSplitPane
  private val titlePanel: PanelImpl = new PanelImpl
  private val buttonsPanel: PanelImpl = new PanelImpl

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
