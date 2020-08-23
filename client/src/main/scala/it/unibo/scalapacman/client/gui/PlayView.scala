package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, Color, Font, GridLayout}

import it.unibo.scalapacman.client.controller.Action.{END_GAME, START_GAME, SUBSCRIBE_TO_EVENTS}
import it.unibo.scalapacman.client.controller.{Action, Controller}
import it.unibo.scalapacman.client.event.{GameUpdate, NewKeyMap, PacmanEvent, PacmanSubscriber}
import it.unibo.scalapacman.client.input.{KeyMap, UserInput}
import it.unibo.scalapacman.client.gui.View.MENU
import it.unibo.scalapacman.client.map.{ElementsCode, PacmanMap}
import it.unibo.scalapacman.client.map.PacmanMap.PacmanMap
import it.unibo.scalapacman.lib.model.{GameState, LevelState, Map, MapType}
import javax.swing.{BorderFactory, JButton, JComponent, JLabel, SwingConstants}

object PlayView {
  def apply()(implicit controller: Controller, viewChanger: ViewChanger): PlayView = new PlayView()
}

class PlayView(implicit controller: Controller, viewChanger: ViewChanger) extends PanelImpl {
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
  private val STARTING_LIVES_COUNT: Int = 1
  private val STARTING_POINTS_COUNT: Int = 0
  private val START_MESSAGE: String = "Per una nuova partita, cliccare sul pulsante 'Inizia partita'"
  private val GOOD_LUCK: String = "Buona fortuna!"
  private val VICTORY_MESSAGE: String = "Vittoria!"
  private val GAME_OVER_MESSAGE: String = "Game Over!"
  private val SCORE_MESSAGE: String = "Punteggio"

  /* GameCanvas constants */
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

  private var _map: Option[PacmanMap] = None
  private var _gameState: Option[GameState] = None
  private var _gameRunning: Boolean = false

  private val IFW = JComponent.WHEN_IN_FOCUSED_WINDOW

  private val livesCount: JLabel = createLabel(STARTING_LIVES_COUNT.toString)
  private val scoreCount: JLabel = createLabel(STARTING_POINTS_COUNT.toString)

  private val scoreLabel: JLabel = createLabel(SCORE_LABEL)
  private val livesLabel: JLabel = createLabel(LIVES_LABEL)

  private val userMessage: JLabel = createLabel(START_MESSAGE)
  private val gameCanvas: GameCanvas = initGameCanvas()

  private val startGameButton: JButton = createButton(START_GAME_BUTTON_LABEL)
  private val endGameButton: JButton = createButton(END_GAME_BUTTON_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  scoreLabel setHorizontalAlignment SwingConstants.CENTER
  livesLabel setHorizontalAlignment SwingConstants.CENTER

  scoreCount setHorizontalAlignment SwingConstants.CENTER
  livesCount setHorizontalAlignment SwingConstants.CENTER

  scoreLabel setFont new Font(MAIN_FONT_NAME, Font.BOLD, MAIN_LABELS_FONT)
  livesLabel setFont new Font(MAIN_FONT_NAME, Font.BOLD, MAIN_LABELS_FONT)

  scoreCount setFont new Font(MAIN_FONT_NAME, Font.BOLD, SUB_LABELS_FONT)
  livesCount setFont new Font(MAIN_FONT_NAME, Font.BOLD, SUB_LABELS_FONT)

  userMessage setFont new Font(MAIN_FONT_NAME, Font.BOLD, MAIN_LABELS_FONT)

  startGameButton addActionListener (_ => {
    askToController(START_GAME, None)
    gameCanvas start()
    _gameRunning = true
    _map = None
    _gameState = None
    userMessage setText GOOD_LUCK
    updateGameView(PacmanMap.toPacmanMap(Map.create(MapType.CLASSIC)), GameState(0), gameCanvas, scoreCount)
  })

  endGameButton addActionListener (_ => {
    updateGameView(_map.getOrElse(Nil), _gameState.getOrElse(GameState(score = 0)).copy(levelState = LevelState.DEFEAT), gameCanvas, scoreCount)
//    askToController(END_GAME, None)
  })

  backButton addActionListener (_ => {
    askToController(END_GAME, None)
    viewChanger.changeView(MENU)
    gameCanvas stop()
    // Pulisco l'area di gioco
    gameCanvas setText ""
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
  playPanel add(userMessage, BorderLayout.PAGE_START)
  playPanel add(gameCanvas, BorderLayout.CENTER)

  setLayout(new BorderLayout)
  add(labelsPanel, BorderLayout.PAGE_START)
  add(playPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)

  // Applico mappatura di default
  bindKeys(playPanel, controller.model.keyMap)
  // Sottoscrivo ad eventi che pubblicherà il controller
  askToController(SUBSCRIBE_TO_EVENTS, Some(PacmanSubscriber(handlePacmanEvent)))

  def setupView(): Unit = {
    userMessage setText START_MESSAGE
    updateScore(0, scoreCount)
  }

  private def initGameCanvas(): GameCanvas = new GameCanvas() {
    setFont(UNIFONT.deriveFont(Font.PLAIN, PLAY_FONT_SIZE))
    setForeground(Color.WHITE)
    setFocusable(false)
    setBackground(BACKGROUND_COLOR)
  }

  private def bindKeys(component: JComponent, keyMap: KeyMap): Unit =
    UserInput.setupUserInput(component.getInputMap(IFW), component.getActionMap, keyMap)

  private def updateGameView(map: PacmanMap, gameState: GameState, gameCanvas: GameCanvas, scoreCount: JLabel): Unit = {
    updateScore(gameState.score, scoreCount)
    printMap(map, gameState, gameCanvas)
    gameState.levelState match {
      case LevelState.DEFEAT | LevelState.VICTORY => handleEndGame(gameState)
      case _ => Unit
    }
  }

  private def updateScore(score: Int, scoreCount: JLabel): Unit = scoreCount.setText(score.toString)

  private def printMap(map: PacmanMap, gameState: GameState, gameCanvas: GameCanvas): Unit = if (!_map.contains(map)) {
    _map = Some(map)
    doPrint(map, gameState, gameCanvas)
  }

  // TODO sfruttare gameState per cambiare colore ai fantasmi?
  private def doPrint(map: PacmanMap, gameState: GameState, gameCanvas: GameCanvas): Unit = {
    def styleGetter(name: String): Option[ElementStyle] = elementStyles.find(style => style.styleName == name)

    gameCanvas setText (
      (for (x <- map.head.indices;
            y <- map.indices;
            tile <- Some(map(y)(x))
            ) yield ((x, y), (tile, retrieveStyle(tile, styleGetter)))) toMap
      )
  }

  private def handleEndGame(gameState: GameState): Unit = {
    _gameRunning = false
    userMessage setText getEndMessage(gameState)
    askToController(END_GAME, None)
  }

  private def getEndMessage(gameState: GameState): String = gameState.levelState match {
    case LevelState.VICTORY => s"$VICTORY_MESSAGE $SCORE_MESSAGE: ${gameState.score}"
    case LevelState.DEFEAT => s"$GAME_OVER_MESSAGE $SCORE_MESSAGE: ${gameState.score}"
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
    case GameUpdate(map, gameState) if _gameRunning =>
      _gameState = Some(gameState)
      updateGameView(map, gameState, gameCanvas, scoreCount)
    case NewKeyMap(keyMap) => bindKeys(playPanel, keyMap)
    case _ => Unit
  }

  private def askToController(action: Action, param: Option[Any]): Unit =
    new Thread() {
      override def run(): Unit = controller.handleAction(action, param)
    } start()
}
