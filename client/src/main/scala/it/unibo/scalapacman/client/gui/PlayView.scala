package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, Color, Font, GridLayout}
import java.util.{Timer, TimerTask}

import it.unibo.scalapacman.client.controller.Action.{END_GAME, PAUSE_RESUME, SUBSCRIBE_TO_EVENTS}
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.event.{GamePaused, GameStarted, GameUpdate, NetworkIssue, NewKeyMap, PacmanEvent, PacmanSubscriber}
import it.unibo.scalapacman.client.input.{KeyMap, UserInput}
import it.unibo.scalapacman.client.gui.View.MENU
import it.unibo.scalapacman.client.map.PacmanMap
import it.unibo.scalapacman.client.map.PacmanMap.PacmanMap
import it.unibo.scalapacman.common.CommandType
import it.unibo.scalapacman.lib.model.{GameState, LevelState, Map, MapType}
import javax.swing.{BorderFactory, JButton, JComponent, JLabel, SwingConstants}

// scalastyle:off multiple.string.literals

object PlayView {
  def apply()(implicit controller: Controller, viewChanger: ViewChanger): PlayView = new PlayView()
}

/**
 * Schermata di gioco, qui l'utente può giocare al gioco di Pacman utilizzando la configurazione tasti di default
 * o quella che ha personalizzato dalla relativa schermata.
 *
 * @param controller il riferimento al componente Controller
 * @param viewChanger il riferimento al componente che gestisce il cambio schermata
 */
class PlayView(implicit controller: Controller, viewChanger: ViewChanger) extends PanelImpl with AskToController {
  private val SCORE_LABEL: String = "Punteggio"
  private val LIVES_LABEL: String = "Vite"
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
  private val GOOD_LUCK_MESSAGE: String = "Buona fortuna!"
  private val PAUSED_MESSAGE: String = "Gioco in pausa..."
  private val RESUME_MESSAGE: String = "Gioco ripreso"
  private val VICTORY_MESSAGE: String = "Vittoria!"
  private val GAME_OVER_MESSAGE: String = "Game Over!"
  private val GAME_END_MESSAGE: String = "Gioco terminato."
  private val SCORE_MESSAGE: String = "Punteggio"

  /* GameCanvas constants */
  private val FONT_PATH = "font/unifont/unifont.ttf"
  private val UNIFONT: Font = loadFont(FONT_PATH)
  private val PLAY_FONT_SIZE: Float = 24f

  private var _map: Option[PacmanMap] = None
  private var _gameState: Option[GameState] = None
  private var _gameRunning: Boolean = false

  private val IFW = JComponent.WHEN_IN_FOCUSED_WINDOW

  private val livesCount: JLabel = createLabel(STARTING_LIVES_COUNT.toString)
  private val scoreCount: JLabel = createLabel(STARTING_POINTS_COUNT.toString)

  private val scoreLabel: JLabel = createLabel(SCORE_LABEL)
  private val livesLabel: JLabel = createLabel(LIVES_LABEL)

  private val userMessage: JLabel = createLabel("")
  private val gameCanvas: GameCanvas = initGameCanvas()

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

  endGameButton addActionListener (_ => {
    updateGameView(_map.getOrElse(Nil), _gameState.getOrElse(GameState(score = 0)).copy(levelState = LevelState.DEFEAT), gameCanvas, scoreCount)
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

//  buttonsPanel add startGameButton
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

  /**
   * Prepara il messaggio di benvenuto all'utente ed azzera il punteggio visualizzato
   */
  def setupView(): Unit = {
    userMessage setText ""
    updateScore(0, scoreCount)
  }

  /**
   * Istanza il componente su cui viene disegnato il gioco e che è incaricato di aggiornare il disegno
   * @return il GameCanvas
   */
  private def initGameCanvas(): GameCanvas = new GameCanvas() {
    setFont(UNIFONT.deriveFont(Font.PLAIN, PLAY_FONT_SIZE))
    setForeground(Color.WHITE)
    setFocusable(false)
    setBackground(BACKGROUND_COLOR)
  }

  /**
   * Esegue le operazioni preliminari quando l'utente inizia una nuova partita
   */
  private def gameStarted(): Unit = {
    gameCanvas start()
    _gameRunning = true
    _map = None
    _gameState = None
    userMessage setText GOOD_LUCK_MESSAGE
    updateGameView(PacmanMap.toPacmanMap(Map.create(MapType.CLASSIC)), GameState(0), gameCanvas, scoreCount)
//    delayedResume() // Il gioco non parte più in pausa
  }

  /**
   * Effettua l'invio temporizzato del comando di riprisa del gioco ad inizio partita
   */
  private def delayedResume(): Unit = {
    val t = new Timer
    t.schedule(new TimerTask() {
      override def run(): Unit = {
        askToController(PAUSE_RESUME, Some(CommandType.RESUME))
        t.cancel()
      }
    }, GAME_RESUME_TIME_DELAY)
  }

  /**
   * Imposta il comportamento del componente rispetto ad una configurazione dei tasti
   * @param component il componente da configurare
   * @param keyMap la configurazione dei tasti
   */
  private def bindKeys(component: JComponent, keyMap: KeyMap): Unit =
    UserInput.setupUserInput(component.getInputMap(IFW), component.getActionMap, keyMap)

  /**
   * Effettua le operazioni per aggiornare l'interfaccia utente
   *
   * @param map la nuova mappa da disegnare
   * @param gameState il nuovo stato della partita
   * @param gameCanvas il componente su cui disegnare la mappa
   * @param scoreCount la JLabel che mostra il punteggio
   */
  private def updateGameView(map: PacmanMap, gameState: GameState, gameCanvas: GameCanvas, scoreCount: JLabel): Unit = {
    updateScore(gameState.score, scoreCount)
    printMap(map, gameState, gameCanvas)
    gameState.levelState match {
      case LevelState.DEFEAT | LevelState.VICTORY => handleEndGame(gameState)
      case _ => Unit
    }
  }

  private def updateScore(score: Int, scoreCount: JLabel): Unit = scoreCount.setText(score.toString)

  /**
   * Se la mappa è diversa rispetto allo stato precedente, invoca il ridisegno della mappa
   *
   * @param map la nuova mappa da disegnare
   * @param gameState il nuovo stato della partita
   * @param gameCanvas il componente su cui disegnare la mappa
   */
  private def printMap(map: PacmanMap, gameState: GameState, gameCanvas: GameCanvas): Unit = if (!_map.contains(map)) {
    _map = Some(map)
    doPrint(map, gameState, gameCanvas)
  }

  /**
   * Disegna la mappa sul GameCanvas
   *
   * @param map la nuova mappa da disegnare
   * @param gameState il nuovo stato della partita
   * @param gameCanvas il componente su cui disegnare la mappa
   */
  private def doPrint(map: PacmanMap, gameState: GameState, gameCanvas: GameCanvas): Unit = {
    gameCanvas.setText(
      (for (x <- map.head.indices;
            y <- map.indices;
            tile <- Some(map(y)(x))
            ) yield ((x, y), (tile._1, tile._2.map(ResolvedElementStyle(_, gameState))))) toMap
      )
  }

  /**
   * Esegue le operazioni nel qual caso la partita sia terminata per vittoria o sconfitta dell'utente
   *
   * @param gameState lo stato della partita
   */
  private def handleEndGame(gameState: GameState): Unit = {
    _gameRunning = false
    userMessage setText getEndMessage(gameState)
    askToController(END_GAME, None)
  }

  /**
   * Genera il messaggio di fine partita da mostrare all'utente
   *
   * @param gameState lo stato della partita
   * @return il messaggio generato
   */
  private def getEndMessage(gameState: GameState): String = gameState.levelState match {
    case LevelState.VICTORY => s"$VICTORY_MESSAGE $SCORE_MESSAGE: ${gameState.score}"
    case LevelState.DEFEAT => s"$GAME_OVER_MESSAGE $SCORE_MESSAGE: ${gameState.score}"
  }

  /**
   * Gestisce gli eventi PacmanEvent pubblicati dal Controller
   *
   * @param pe il PacmanEvent ricevuto
   */
  private def handlePacmanEvent(pe: PacmanEvent): Unit = pe match {
    case GameUpdate(map, gameState) if _gameRunning =>
      _gameState = Some(gameState)
      updateGameView(map, gameState, gameCanvas, scoreCount)
    case GamePaused(true) =>
      userMessage setText PAUSED_MESSAGE
      _gameRunning = false
    case GamePaused(false) =>
      userMessage setText RESUME_MESSAGE
      _gameRunning = true
    case GameStarted() => gameStarted()
    case NewKeyMap(keyMap) => bindKeys(playPanel, keyMap)
    case NetworkIssue(false, info) => userMessage setText s"$PAUSED_MESSAGE $info"
    case NetworkIssue(true, info) => userMessage setText s"$GAME_END_MESSAGE $info"
    case _ => Unit
  }
}

// scalastyle:on multiple.string.literals
