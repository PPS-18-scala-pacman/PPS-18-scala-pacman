package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, Color, Font, GridLayout}

import it.unibo.scalapacman.client.controller.Action.{END_GAME, START_GAME, SUBSCRIBE_TO_EVENTS}
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.event.{GameUpdate, NewKeyMap, PacmanEvent, PacmanSubscriber}
import it.unibo.scalapacman.client.gui.GameCanvas.CompositeMessage
import it.unibo.scalapacman.client.input.{KeyMap, UserInput}
import it.unibo.scalapacman.client.gui.View.MENU
import it.unibo.scalapacman.client.map.ElementsCode
import it.unibo.scalapacman.client.map.PacmanMap.PacmanMap
import it.unibo.scalapacman.lib.model.GameState
import javax.swing.{BorderFactory, JButton, JComponent, JLabel, SwingConstants}

object PlayView {
  def apply()(implicit controller: Controller, viewChanger: ViewChanger): PlayView = new PlayView()
}

class PlayView(implicit controller: Controller, viewChanger: ViewChanger) extends PanelImpl {
  private val TITLE_LABEL: String = "Play View"
  private val SCORE_LABEL: String = "Punteggio"
  private val LIVES_LABEL: String = "Vite"
  private val START_GAME_BUTTON_LABEL: String = "Inizia partita"
  private val END_GAME_BUTTON_LABEL: String = "Fine partita"
  private val BACK_BUTTON_LABEL: String = "Indietro"
  private val PLAY_PANEL_BORDER: Int = 5
  private val MAIN_LABELS_FONT: Int = 24
  private val SUB_LABELS_FONT: Int = 16
  private val EMPTY_BORDER_SIZE_Y: Int = 0
  private val EMPTY_BORDER_SIZE_X: Int = 5
  private val LABELS_LAYOUT_ROWS: Int = 2
  private val LABELS_LAYOUT_COLS: Int = 2
  private val STARTING_LIVES_COUNT: Int = 3
  private val STARTING_POINTS_COUNT: Int = 0
  private val START_MESSAGE: String = "Per iniziare una nuova partita, cliccare sul pulsante 'Inizia partita'"
  private val START_MESSAGE_COMPOSITE: CompositeMessage =
    ((0, 0), (START_MESSAGE.split(", ")(0), None)) :: ((0, 1), (START_MESSAGE.split(", ")(1), None)) :: Nil toIndexedSeq
  private val USER_STOP_MESSAGE: String = "Partita interrotta dall'utente"
  private val GAME_END_MESSAGE: String = "Game Over"

  /* TextPane constants */
  private val FONT_PATH = "font/unifont/unifont.ttf"
  private val UNIFONT: Font = loadFont(FONT_PATH)
  private val PLAY_FONT_SIZE: Float = 24f
  private val PACMAN_SN = "pacman"
  private val PACMAN_COLOR: Color = Color.YELLOW
  private val GHOST_SN = "ghost"
  private val GHOST_COLOR: Color = Color.RED
  private val DOT_SN = "dot"
  private val DOT_COLOR: Color = Color.WHITE
  private val WALL_SN = "wall"
  private val WALL_COLOR: Color = Color.BLUE

  private val elementStyles: List[ElementStyle] =
    ElementStyle(PACMAN_SN, PACMAN_COLOR) :: ElementStyle(DOT_SN, DOT_COLOR) ::
      ElementStyle(GHOST_SN, GHOST_COLOR) :: ElementStyle(WALL_SN, WALL_COLOR) :: Nil

  private var _prevMap: Option[PacmanMap] = None

  private val IFW = JComponent.WHEN_IN_FOCUSED_WINDOW

  private val livesCount: JLabel = createLabel(STARTING_LIVES_COUNT.toString)
  private val scoreCount: JLabel = createLabel(STARTING_POINTS_COUNT.toString)

  private val placeholderLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val scoreLabel: JLabel = createLabel(SCORE_LABEL)
  private val livesLabel: JLabel = createLabel(LIVES_LABEL)
  private val startGameButton: JButton = createButton(START_GAME_BUTTON_LABEL)
  private val endGameButton: JButton = createButton(END_GAME_BUTTON_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)
  private val gameCanvas: GameCanvas = initGameCanvas()

  placeholderLabel setHorizontalAlignment SwingConstants.CENTER
  scoreLabel setHorizontalAlignment SwingConstants.CENTER
  livesLabel setHorizontalAlignment SwingConstants.CENTER

  scoreCount setHorizontalAlignment SwingConstants.CENTER
  livesCount setHorizontalAlignment SwingConstants.CENTER

  scoreLabel setFont new Font(MAIN_FONT_NAME, Font.BOLD, MAIN_LABELS_FONT)
  livesLabel setFont new Font(MAIN_FONT_NAME, Font.BOLD, MAIN_LABELS_FONT)

  scoreCount setFont new Font(MAIN_FONT_NAME, Font.BOLD, SUB_LABELS_FONT)
  livesCount setFont new Font(MAIN_FONT_NAME, Font.BOLD, SUB_LABELS_FONT)

  startGameButton addActionListener (_ => controller.handleAction(START_GAME, None))
  endGameButton addActionListener (_ => {
    gameCanvas setText USER_STOP_MESSAGE
    controller.handleAction(END_GAME, None)
    _prevMap = None
  })
  backButton addActionListener (_ => {
    controller.handleAction(END_GAME, None)
    viewChanger.changeView(MENU)
    updateScore(0, scoreCount)
    gameCanvas setText START_MESSAGE_COMPOSITE
  })

  private val playPanel: PanelImpl = PanelImpl()
  private val buttonsPanel: PanelImpl = PanelImpl()
  private val labelsPanel: PanelImpl = PanelImpl()

  buttonsPanel add startGameButton
  buttonsPanel add endGameButton
  buttonsPanel add backButton

  labelsPanel setLayout new GridLayout(LABELS_LAYOUT_ROWS, LABELS_LAYOUT_COLS)
  labelsPanel setBorder BorderFactory.createEmptyBorder(EMPTY_BORDER_SIZE_Y, EMPTY_BORDER_SIZE_X, EMPTY_BORDER_SIZE_Y, EMPTY_BORDER_SIZE_X)
  labelsPanel add livesLabel
  labelsPanel add scoreLabel
  labelsPanel add livesCount
  labelsPanel add scoreCount

  playPanel setLayout new BorderLayout
  playPanel setBorder BorderFactory.createLineBorder(Color.white, PLAY_PANEL_BORDER)
  playPanel add(gameCanvas, BorderLayout.CENTER)

  setLayout(new BorderLayout)
  add(labelsPanel, BorderLayout.PAGE_START)
  add(playPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)

  // Applico mappatura di default
  bindKeys(playPanel, controller.model.keyMap)
  // Sottoscrivo ad eventi che pubblicherà il controller
  controller.handleAction(SUBSCRIBE_TO_EVENTS, Some(PacmanSubscriber(handlePacmanEvent)))

  def setupView(): Unit = {
    gameCanvas setText START_MESSAGE_COMPOSITE
    gameCanvas.start()
  }

  private def initGameCanvas(): GameCanvas = new GameCanvas() {
    setFont(UNIFONT.deriveFont(Font.PLAIN, PLAY_FONT_SIZE))
    setForeground(Color.WHITE)
    setFocusable(false)
    setBackground(BACKGROUND_COLOR)
  }

  private def bindKeys(component: JComponent, keyMap: KeyMap): Unit =
    UserInput.setupUserInput(component.getInputMap(IFW), component.getActionMap, keyMap)

  private def updateScore(score: Int, scoreCount: JLabel): Unit = scoreCount.setText(score.toString)

  private def printMap(map: PacmanMap, gameState: GameState, gameCanvas: GameCanvas): Unit = if (!_prevMap.contains(map)) {
    _prevMap = Some(map)
    doPrint(map, gameState, gameCanvas)
  }

  // TODO sfruttare gameState per cambiare colore ai fantasmi
  private def doPrint(map: PacmanMap, gameState: GameState, gameCanvas: GameCanvas): Unit = {
    gameCanvas setText (for (x <- map.head.indices;
                             y <- map.indices
                             ) yield ((x, y), (map(y)(x), retrieveStyle(map(y)(x), name => elementStyles.find(style => style.styleName == name)))))
  }

  // scalastyle:off cyclomatic.complexity
  private def retrieveStyle(elem: String, styleGetter: String => Option[ElementStyle]): Option[ElementStyle] = elem match {
    case _ if ElementsCode.matchDot(elem) || ElementsCode.matchFruit(elem) => styleGetter(DOT_SN)
    case _ if ElementsCode.matchPacman(elem) => styleGetter(PACMAN_SN)
    case _ if elem.length == 2 && ElementsCode.matchGhost(elem.substring(0, 1)) => styleGetter(GHOST_SN)
    case ElementsCode.WALL_CODE => styleGetter(WALL_SN)
    case ElementsCode.EMPTY_SPACE_CODE => None
  }
  // scalastyle:on cyclomatic.complexity

  private def handlePacmanEvent(pe: PacmanEvent): Unit = pe match {
    case GameUpdate(map, gameState) => updateScore(gameState.score, scoreCount); printMap(map, gameState, gameCanvas)
    case NewKeyMap(keyMap) => bindKeys(playPanel, keyMap)
    case _ => Unit
  }
}
