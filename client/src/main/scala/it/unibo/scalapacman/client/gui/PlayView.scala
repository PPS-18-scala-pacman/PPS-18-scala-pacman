package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, Color, Font, GridLayout}

import it.unibo.scalapacman.client.controller.Action.{END_GAME, START_GAME, SUBSCRIBE_TO_GAME_UPDATES}
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.event.{GameUpdate, PacmanEvent, PacmanSubscriber}
import it.unibo.scalapacman.client.input.{KeyBinder, KeyMap, UserInput}
import it.unibo.scalapacman.client.gui.View.MENU
import it.unibo.scalapacman.client.map.ElementsCode
import it.unibo.scalapacman.client.map.PacmanMap.PacmanMap
import javax.swing.text.{Style, StyleConstants, StyleContext, StyledDocument}
import javax.swing.{BorderFactory, JButton, JComponent, JLabel, JTextPane, SwingConstants}

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

  // Stile di default, root degli stili personalizzati che andremo ad aggiungere
  private val default: Style = StyleContext.getDefaultStyleContext.getStyle(StyleContext.DEFAULT_STYLE)
  private var _prevMap: Option[PacmanMap] = None

  private val IFW = JComponent.WHEN_IN_FOCUSED_WINDOW

  private val livesCount: JLabel = createLabel(STARTING_LIVES_COUNT.toString)
  private val scoreCount: JLabel = createLabel(STARTING_POINTS_COUNT.toString)

  private val textPane: JTextPane = initTextPane()
  private val placeholderLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val scoreLabel: JLabel = createLabel(SCORE_LABEL)
  private val livesLabel: JLabel = createLabel(LIVES_LABEL)
  private val startGameButton: JButton = createButton(START_GAME_BUTTON_LABEL)
  private val endGameButton: JButton = createButton(END_GAME_BUTTON_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  // Applico mappatura di default
  bindKeys(textPane)(controller.getKeyMap)
  // Sottoscrivo ad eventi che pubblicherà il controller
  controller.handleAction(SUBSCRIBE_TO_GAME_UPDATES, Some(PacmanSubscriber(handlePacmanEvent)))

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

  labelsPanel setLayout new GridLayout(LABELS_LAYOUT_ROWS,LABELS_LAYOUT_COLS)
  labelsPanel setBorder BorderFactory.createEmptyBorder(EMPTY_BORDER_SIZE_Y, EMPTY_BORDER_SIZE_X, EMPTY_BORDER_SIZE_Y, EMPTY_BORDER_SIZE_X)
  labelsPanel add livesLabel
  labelsPanel add scoreLabel
  labelsPanel add livesCount
  labelsPanel add scoreCount

  playPanel setLayout new BorderLayout
  playPanel setBorder BorderFactory.createLineBorder(Color.white, PLAY_PANEL_BORDER)
  playPanel add (textPane, BorderLayout.CENTER)

  setLayout(new BorderLayout)
  add(labelsPanel, BorderLayout.PAGE_START)
  add(playPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)

  private def initTextPane(): JTextPane = {
    val tp = new JTextPane() {
      setFont(UNIFONT.deriveFont(Font.PLAIN, PLAY_FONT_SIZE))
      setForeground(Color.WHITE)
      setText(START_MESSAGE)
      setEditable(false)
      setFocusable(false)
      setBackground(BACKGROUND_COLOR)
    }

    setupStyles(tp.getStyledDocument, elementStyles)
    tp
  }

  private def bindKeys(component: JComponent)(keyMap: KeyMap): Unit =
    UserInput.setupUserInput(component.getInputMap(IFW), component.getActionMap, keyMap)

  def applyKeyBinding(keyMap: KeyMap): Unit = bindKeys(textPane)(keyMap)

  /**
   * Aggiunge le informazioni sullo stile al documento
   * @param doc             il documento di cui espandere lo stile
   * @param elementStyles   la lista di stili personalizzati da inserire
   */
  private def setupStyles(doc: StyledDocument, elementStyles: List[ElementStyle]): Unit = elementStyles foreach { elementStyle =>
    StyleConstants.setForeground(doc.addStyle(elementStyle.styleName, default), elementStyle.foregroundColor)
  }

  private def updateScore(score: Int, scoreCount: JLabel): Unit = scoreCount.setText(score.toString)

  private def updateMessage(message: String, textPane: JTextPane): Unit = textPane.setText(message)

  private def printMap(map: PacmanMap, tp: JTextPane): Unit = _prevMap match {
    case Some(`map`) => // stessa mappa di prima, non faccio nulla
    case _ =>
      _prevMap = Some(map)
      updateMessage("", tp)
      doPrint(map, tp.getStyledDocument)
  }

  private def doPrint(map: PacmanMap, doc: StyledDocument): Unit = map foreach { row =>
    row foreach {
      case elem@ElementsCode.WALL_CODE => insertInDocument(doc, elem, doc.getStyle(WALL_SN))
      case elem@(ElementsCode.DOT_CODE | ElementsCode.ENERGIZER_DOT_CODE) => insertInDocument(doc, elem, doc.getStyle(DOT_SN))
      case elem@ElementsCode.EMPTY_SPACE_CODE => insertInDocument(doc, elem, null) // scalastyle:ignore null
      case elem: String if ElementsCode.matchPacman(elem) => insertInDocument(doc, elem, doc.getStyle(PACMAN_SN))
      case elem: String if elem.length == 2 && ElementsCode.matchGhost(elem.substring(0, 1)) => insertInDocument(doc, elem, doc.getStyle(GHOST_SN))
      case elem: String if ElementsCode.matchFruit(elem) => insertInDocument(doc, elem, doc.getStyle(DOT_SN))
    }
    // A fine riga aggiunge un newline per disegnare correttamente la mappa
    insertInDocument(doc, "\n", null)// scalastyle:ignore null
  }

  private def insertInDocument(doc: StyledDocument, text: String, style: Style): Unit = doc.insertString(doc.getLength, text, style)

  private def handlePacmanEvent(pe: PacmanEvent): Unit = pe match {
    case GameUpdate(map, score) => updateScore(score, scoreCount); printMap(map, textPane)
  }
}
