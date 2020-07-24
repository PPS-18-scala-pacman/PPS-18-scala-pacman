package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, Color, Font, GridLayout}

import it.unibo.scalapacman.client.controller.Action.{CHANGE_VIEW, END_GAME, START_GAME}
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.input.{KeyBinder, KeyMap, UserInput}
import it.unibo.scalapacman.client.gui.View.MENU
import javax.swing.{BorderFactory, JButton, JComponent, JLabel, JTextPane, SwingConstants}

object PlayView {
  def apply()(implicit controller: Controller, viewChanger: ViewChanger): PlayView = new PlayView()
}

class PlayView(implicit controller: Controller, viewChanger: ViewChanger) extends PanelImpl with KeyBinder {
  private val TITLE_LABEL: String = "Play View"
  private val POINTS_LABEL: String = "Punteggio"
  private val LIVES_LABEL: String = "Vite"
  private val START_GAME_BUTTON_LABEL: String = "Inizia partita"
  private val END_GAME_BUTTON_LABEL: String = "Fine partita"
  private val BACK_BUTTON_LABEL: String = "Indietro"
  private val PLAY_PANEL_BORDER: Int = 5
  private val MAIN_LABELS_FONT: Int = 24
  private val SUB_LABELS_FONT: Int = 16
  private val EMPTY_BORDER_SIZE_Y: Int = 10
  private val EMPTY_BORDER_SIZE_X: Int = 5
  private val LABELS_LAYOUT_ROWS: Int = 2
  private val LABELS_LAYOUT_COLS: Int = 2
  private val STARTING_LIVES_COUNT: Int = 3
  private val STARTING_POINTS_COUNT: Int = 0

  private val IFW = JComponent.WHEN_IN_FOCUSED_WINDOW

  private val livesCount: JLabel = createLabel(STARTING_LIVES_COUNT.toString)
  private val pointsCount: JLabel = createLabel(STARTING_POINTS_COUNT.toString)

  private val textPane: JTextPane = initTextPane
  private val placeholderLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val pointsLabel: JLabel = createLabel(POINTS_LABEL)
  private val livesLabel: JLabel = createLabel(LIVES_LABEL)
  private val startGameButton: JButton = createButton(START_GAME_BUTTON_LABEL)
  private val endGameButton: JButton = createButton(END_GAME_BUTTON_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  // Applico mappatura di default
  bindKeys(textPane)(controller.getKeyMap)

  placeholderLabel setHorizontalAlignment SwingConstants.CENTER
  pointsLabel setHorizontalAlignment SwingConstants.CENTER
  livesLabel setHorizontalAlignment SwingConstants.CENTER

  pointsCount setHorizontalAlignment SwingConstants.CENTER
  livesCount setHorizontalAlignment SwingConstants.CENTER

  pointsLabel setFont new Font(MAIN_FONT_NAME, Font.BOLD, MAIN_LABELS_FONT)
  livesLabel setFont new Font(MAIN_FONT_NAME, Font.BOLD, MAIN_LABELS_FONT)

  pointsCount setFont new Font(MAIN_FONT_NAME, Font.BOLD, SUB_LABELS_FONT)
  livesCount setFont new Font(MAIN_FONT_NAME, Font.BOLD, SUB_LABELS_FONT)

  startGameButton addActionListener (_ => controller.handleAction(START_GAME, None))
  endGameButton addActionListener (_ => controller.handleAction(END_GAME, None))
  backButton addActionListener (_ => {
    controller.handleAction(END_GAME, None)
    viewChanger.changeView(MENU)
  })

  private val playPanel: PanelImpl = PanelImpl()
  private val buttonsPanel: PanelImpl = PanelImpl()
  private val labelsPanel: PanelImpl = PanelImpl()

  buttonsPanel add startGameButton
  buttonsPanel add endGameButton
  buttonsPanel add backButton

  labelsPanel setLayout new GridLayout(LABELS_LAYOUT_ROWS,LABELS_LAYOUT_COLS)
  labelsPanel setBorder BorderFactory.createEmptyBorder(EMPTY_BORDER_SIZE_Y, EMPTY_BORDER_SIZE_X, EMPTY_BORDER_SIZE_Y, EMPTY_BORDER_SIZE_X)
  labelsPanel add livesLabel
  labelsPanel add pointsLabel
  labelsPanel add livesCount
  labelsPanel add pointsCount

  playPanel setLayout new BorderLayout
  playPanel setBorder BorderFactory.createLineBorder(Color.white, PLAY_PANEL_BORDER)
  playPanel add (textPane, BorderLayout.CENTER)

  setLayout(new BorderLayout)
  add(labelsPanel, BorderLayout.PAGE_START)
  add(playPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)

  private def initTextPane: JTextPane = {
    val tp = new JTextPane() {
      setFont(UNIFONT.deriveFont(Font.PLAIN, 16f))
      setText("Test")
      setEditable(false)
      setFocusable(false)
      setBackground(BACKGROUND_COLOR)
    }

    val doc = tp.getStyledDocument
    // Personalizza stile documento
    addStylesToDocument(doc)

    tp
  }

  private def bindKeys(component: JComponent)(keyMap: KeyMap): Unit =
    UserInput.setupUserInput(component.getInputMap(IFW), component.getActionMap, keyMap)

  def applyKeyBinding(keyMap: KeyMap): Unit = bindKeys(textPane)(keyMap)
}
