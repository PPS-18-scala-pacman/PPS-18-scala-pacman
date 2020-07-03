package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, Color, Component, Font, GridLayout}

import it.unibo.scalapacman.client.gui.GUI.ViewsName
import javax.swing.{BorderFactory, JButton, JLabel, SwingConstants}

class PlayView extends PanelImpl {
  private val TITLE_LABEL: String = "Play View"
  private val POINTS_LABEL: String = "Punteggio"
  private val LIVES_LABEL: String = "Vite"
  private val BACK_BUTTON_LABEL: String = "Indietro"
  private val PLAY_PANEL_BORDER: Int = 5
  private val MAIN_LABELS_FONT: Int = 24
  private val SUB_LABELS_FONT: Int = 16
  private val EMPTY_BORDER_SIZE_Y: Int = 10
  private val EMPTY_BORDER_SIZE_X: Int = 5
  private val STARTING_LIVES_COUNT: Int = 3
  private val STARTING_POINTS_COUNT: Int = 0

  private var livesCount: JLabel = createLabel(STARTING_LIVES_COUNT.toString)
  private var pointsCount: JLabel = createLabel(STARTING_POINTS_COUNT.toString)

  private val placeholderLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val pointsLabel: JLabel = createLabel(POINTS_LABEL)
  private val livesLabel: JLabel = createLabel(LIVES_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  backButton setAlignmentX Component.CENTER_ALIGNMENT

  placeholderLabel setHorizontalAlignment SwingConstants.CENTER
  pointsLabel setHorizontalAlignment SwingConstants.CENTER
  livesLabel setHorizontalAlignment SwingConstants.CENTER

  pointsCount setHorizontalAlignment SwingConstants.CENTER
  livesCount setHorizontalAlignment SwingConstants.CENTER

  pointsLabel setFont new Font(MAIN_FONT_NAME, Font.BOLD, MAIN_LABELS_FONT)
  livesLabel setFont new Font(MAIN_FONT_NAME, Font.BOLD, MAIN_LABELS_FONT)

  pointsCount setFont new Font(MAIN_FONT_NAME, Font.BOLD, SUB_LABELS_FONT)
  livesCount setFont new Font(MAIN_FONT_NAME, Font.BOLD, SUB_LABELS_FONT)

  backButton addActionListener (_ => GUI.changeView(ViewsName.MENU_VIEW))

  private val playPanel: PanelImpl = new PanelImpl
  private val buttonsPanel: PanelImpl = new PanelImpl
  private val labelsPanel: PanelImpl = new PanelImpl

  buttonsPanel add backButton

  labelsPanel setLayout new GridLayout(2,2)
  labelsPanel setBorder BorderFactory.createEmptyBorder(EMPTY_BORDER_SIZE_Y, EMPTY_BORDER_SIZE_X, EMPTY_BORDER_SIZE_Y, EMPTY_BORDER_SIZE_X)
  labelsPanel add livesLabel
  labelsPanel add pointsLabel
  labelsPanel add livesCount
  labelsPanel add pointsCount

  playPanel setLayout new BorderLayout
  playPanel setBorder BorderFactory.createLineBorder(Color.white, PLAY_PANEL_BORDER)
  playPanel add (placeholderLabel, BorderLayout.CENTER)

  setLayout(new BorderLayout)
  add(labelsPanel, BorderLayout.PAGE_START)
  add(playPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)
}
