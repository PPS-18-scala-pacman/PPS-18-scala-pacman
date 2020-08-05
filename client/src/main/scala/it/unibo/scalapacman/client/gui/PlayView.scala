package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, Canvas, Color, Dimension, Font, Graphics2D, GridLayout, RenderingHints}
import java.util.concurrent.Semaphore

import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.controller.Action.{END_GAME, START_GAME, SUBSCRIBE_TO_GAME_UPDATES}
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.event.{GameUpdate, PacmanEvent, PacmanSubscriber}
import it.unibo.scalapacman.client.input.{KeyBinder, KeyMap, UserInput}
import it.unibo.scalapacman.client.gui.View.MENU
import it.unibo.scalapacman.client.map.ElementsCode
import it.unibo.scalapacman.client.map.PacmanMap.PacmanMap
import javax.swing.{BorderFactory, JButton, JComponent, JLabel, SwingConstants}

import scala.collection.concurrent.TrieMap
import scala.collection.immutable

object PlayView {
  def apply()(implicit controller: Controller, viewChanger: ViewChanger): PlayView = new PlayView()
}

class PlayView(implicit controller: Controller, viewChanger: ViewChanger) extends PanelImpl with KeyBinder {
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
  private val USER_STOP_MESSAGE: String = "Partita interrotta dall'utente"
  private val GAME_END_MESSAGE: String = "Game Over"

  /* TextPane constants */
  private val FONT_PATH = "font/unifont/unifont.ttf"
  private val UNIFONT: Font = loadFont(FONT_PATH)
  private val PLAY_FONT_SIZE: Float = 24f
  private val PLAY_BACKGROUND_COLOR: Color = Color.DARK_GRAY
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

  val textPane: GameCanvas = initTextPane()
  private val placeholderLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val scoreLabel: JLabel = createLabel(SCORE_LABEL)
  private val livesLabel: JLabel = createLabel(LIVES_LABEL)
  private val startGameButton: JButton = createButton(START_GAME_BUTTON_LABEL)
  private val endGameButton: JButton = createButton(END_GAME_BUTTON_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

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
    updateMessage(USER_STOP_MESSAGE, textPane)
    controller.handleAction(END_GAME, None)
    _prevMap = None
  })
  backButton addActionListener (_ => {
    controller.handleAction(END_GAME, None)
    viewChanger.changeView(MENU)
    updateMessage(START_MESSAGE, textPane)
    updateScore(0, scoreCount)
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
  playPanel add(textPane, BorderLayout.CENTER)

  setLayout(new BorderLayout)
  add(labelsPanel, BorderLayout.PAGE_START)
  add(playPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)

  // Applico mappatura di default
  bindKeys(playPanel)(controller.getKeyMap)
  // Sottoscrivo ad eventi che pubblicherà il controller
  controller.handleAction(SUBSCRIBE_TO_GAME_UPDATES, Some(PacmanSubscriber(handlePacmanEvent)))

  def setupView(): Unit = {
    textPane.start()
//    updateMessage("", textPane)
    setStartMessage(textPane)
  }

  private def initTextPane(): GameCanvas = new GameCanvas() {
    setFont(UNIFONT.deriveFont(Font.PLAIN, PLAY_FONT_SIZE))
    setForeground(Color.WHITE)
    setFocusable(false)
    setBackground(BACKGROUND_COLOR)
  }

  private def setStartMessage(textPane: GameCanvas): Unit = updateMessage(
    ((0, 0), (START_MESSAGE.split(", ")(0), None)) :: ((0, 1), (START_MESSAGE.split(", ")(1), None)) :: Nil toIndexedSeq,
    textPane
  )

  private def bindKeys(component: JComponent)(keyMap: KeyMap): Unit =
    UserInput.setupUserInput(component.getInputMap(IFW), component.getActionMap, keyMap)

  def applyKeyBinding(keyMap: KeyMap): Unit = bindKeys(playPanel)(keyMap)

  private def updateScore(score: Int, scoreCount: JLabel): Unit = scoreCount.setText(score.toString)

  private def updateMessage(message: String, textPane: GameCanvas): Unit = textPane setText message

  private def updateMessage(message: immutable.IndexedSeq[((Int, Int), (String, Option[ElementStyle]))], textPane: GameCanvas): Unit =
    textPane setText message

  private def printMap(map: PacmanMap, tp: GameCanvas): Unit = if (!_prevMap.contains(map)) {
    _prevMap = Some(map)
    doPrint(map, tp)
  }

  private def doPrint(map: PacmanMap, doc: GameCanvas): Unit = {
    doc setText (for (x <- map.head.indices;
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
    case GameUpdate(map, score) => updateScore(score, scoreCount); printMap(map, textPane)
  }
}

class GameCanvas extends Canvas with Runnable with Logging {

  private val text: TrieMap[(Int, Int), (String, Option[ElementStyle])] = TrieMap.empty
  private var running = false
  private var gameThread: Thread = _
  private val BUFFERS_COUNT = 3
  private val pleaseRender = new Semaphore(0)

  setPreferredSize(new Dimension(WIDTH, HEIGHT))

  def setText(message: String): Unit = {
    text.clear
    setText(((0, 0), (message, None)) :: Nil toIndexedSeq)
  }

  def setText(messages: immutable.IndexedSeq[((Int, Int), (String, Option[ElementStyle]))]): Unit = {
    text ++= messages
    if (pleaseRender.availablePermits() == 0) pleaseRender.release()
  }

  def start(): Unit =
    if (!running) {
      running = true
      gameThread = new Thread(this)
      gameThread.start()
      debug("Game thread started")
    }

  // ends the game
  def stop(): Unit = {
    if (!running) {
      running = false
      var retry = true
      while (retry) try {
        gameThread.join()
        retry = false
        debug("Game thread stopped")
      } catch {
        case e: InterruptedException =>
          debug("Failed sopping game thread, retry in 1 second")
          try Thread.sleep(1000) // scalastyle:ignore magic.number
          catch {
            case e1: InterruptedException =>
              e1.printStackTrace()
          }
      }
    }
  }

  private def render(): Unit = {
    var bs = getBufferStrategy
    if (bs == null) {
      createBufferStrategy(BUFFERS_COUNT)
      bs = getBufferStrategy
    }
    val g2d = bs.getDrawGraphics.create.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    clear(g2d, 0)
    // render here
    g2d.setFont(getFont)
    val metrics = g2d.getFontMetrics()
    for ((indexes, v) <- text.toSeq) {
      g2d.setColor(v._2.map(_.foregroundColor).getOrElse(Color.white))
      g2d.drawString(v._1, indexes._1 * metrics.stringWidth(v._1), (indexes._2 + 1) * (metrics.getAscent.abs + metrics.getDescent.abs))
    }
    //////////////
    g2d.dispose()
    bs.show()
  }

  private def clear(g2d: Graphics2D, shade: Int): Unit = {
    g2d.setColor(new Color(shade, shade, shade))
    g2d.fillRect(0, 0, WIDTH, HEIGHT)
  }

  def run(): Unit = {
    while (running) {
      pleaseRender.acquire()
      render()
//      try Thread.sleep(5) // always a good idea to let is breath a bit
//      catch {
//        case e: InterruptedException => e.printStackTrace()
//      }
    }
  }
}
