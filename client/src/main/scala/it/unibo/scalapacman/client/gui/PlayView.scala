package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, Color, Font, GridLayout}

import it.unibo.scalapacman.client.controller.Action.{END_GAME, START_GAME, SUBSCRIBE_TO_GAME_UPDATES}
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.event.{GameUpdate, PacmanEvent, PacmanSubscriber}
import it.unibo.scalapacman.client.input.{KeyBinder, KeyMap, UserInput}
import it.unibo.scalapacman.client.gui.View.MENU
import it.unibo.scalapacman.client.map.ElementsCharCode
import it.unibo.scalapacman.client.map.PacmanMap.PacmanMap
import javax.swing.text.{Style, StyleConstants, StyleContext, StyledDocument}
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
  private val EMPTY_BORDER_SIZE_Y: Int = 0
  private val EMPTY_BORDER_SIZE_X: Int = 5
  private val LABELS_LAYOUT_ROWS: Int = 2
  private val LABELS_LAYOUT_COLS: Int = 2
  private val STARTING_LIVES_COUNT: Int = 3
  private val STARTING_POINTS_COUNT: Int = 0
  private val START_MESSAGE: String = "Per iniziare una nuova partita, cliccare sul pulsante 'Inizia partita'"

  /* TextPane constants */
  private val FONT_PATH = "font/unifont/unifont.ttf"
  private val UNIFONT: Font = loadFont(FONT_PATH)
  private val PLAY_FONT_SIZE: Float = 24f
  private val PLAY_BACKGROUND_COLOR: Color = Color.DARK_GRAY
  private val PACMAN_SN = "pacman"
  private val PACMAN_COLOR: Color = Color.YELLOW
  private val DOT_SN = "pellet"
  private val DOT_COLOR: Color = Color.WHITE
  private val WALL_SN = "wall"
  private val WALL_COLOR: Color = Color.BLUE

  private val charStyles: List[CharStyle] = CharStyle(PACMAN_SN, PACMAN_COLOR) :: CharStyle(DOT_SN, DOT_COLOR) :: CharStyle(WALL_SN, WALL_COLOR) :: Nil

  // Stile di default, root degli stili personalizzati che andremo ad aggiungere
  private val default: Style = StyleContext.getDefaultStyleContext.getStyle(StyleContext.DEFAULT_STYLE)
  private var _prevMap: Option[PacmanMap] = None

  private val IFW = JComponent.WHEN_IN_FOCUSED_WINDOW

  private val livesCount: JLabel = createLabel(STARTING_LIVES_COUNT.toString)
  private val pointsCount: JLabel = createLabel(STARTING_POINTS_COUNT.toString)

  private val textPane: JTextPane = initTextPane()
  private val placeholderLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val pointsLabel: JLabel = createLabel(POINTS_LABEL)
  private val livesLabel: JLabel = createLabel(LIVES_LABEL)
  private val startGameButton: JButton = createButton(START_GAME_BUTTON_LABEL)
  private val endGameButton: JButton = createButton(END_GAME_BUTTON_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  // Applico mappatura di default
  bindKeys(textPane)(controller.getKeyMap)
  // Sottoscrivo ad eventi che pubblicherà il controller
  controller.handleAction(SUBSCRIBE_TO_GAME_UPDATES, Some(PacmanSubscriber(handlePacmanEvent)))

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
  endGameButton addActionListener (_ => {
    textPane.setText("Partita interrotta dall'utente")
    controller.handleAction(END_GAME, None)
    _prevMap = None
  })
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
  labelsPanel add livesCount
  labelsPanel add pointsLabel
  labelsPanel add pointsCount

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

    setupStyles(tp.getStyledDocument, charStyles)
    tp
  }

  private def bindKeys(component: JComponent)(keyMap: KeyMap): Unit =
    UserInput.setupUserInput(component.getInputMap(IFW), component.getActionMap, keyMap)

  def applyKeyBinding(keyMap: KeyMap): Unit = bindKeys(textPane)(keyMap)

  /**
   * Aggiunge le informazioni sullo stile al documento
   * @param doc         il documento di cui espandere lo stile
   * @param charStyles  la lista di stili personalizzati da inserire
   */
  private def setupStyles(doc: StyledDocument, charStyles: List[CharStyle]): Unit = charStyles foreach { charStyle =>
    StyleConstants.setForeground(doc.addStyle(charStyle.styleName, default), charStyle.foregroundColor)
  }

  private def printMap(map: PacmanMap, tp: JTextPane): Unit = _prevMap match {
    case Some(`map`) => // stessa mappa di prima, non faccio nulla
    case _ =>
      _prevMap = Some(map)
      tp.setText("")
      doPrint(map, tp.getStyledDocument)
  }

  private def doPrint(map: PacmanMap, doc: StyledDocument): Unit = map foreach { row =>
    row foreach {
      case char @ ElementsCharCode.WALL_CODE => insertInDocument(doc, char, doc.getStyle(WALL_SN))
      case char @ (ElementsCharCode.DOT_CODE | ElementsCharCode.ENERGIZED_DOT_CODE) => insertInDocument(doc, char, doc.getStyle(DOT_SN))
      case char @ ElementsCharCode.EMPTY_SPACE_CODE => insertInDocument(doc, char, null)// scalastyle:ignore null
    }
    // A fine riga aggiunge un newline per disegnare correttamente la mappa
    insertInDocument(doc, "\n", null)// scalastyle:ignore null
  }

  private def insertInDocument(doc: StyledDocument, text: String, style: Style): Unit = doc.insertString(doc.getLength, text, style)

  private def handlePacmanEvent(pe: PacmanEvent): Unit = pe match {
    case GameUpdate(map) => printMap(map, textPane)
  }
}
