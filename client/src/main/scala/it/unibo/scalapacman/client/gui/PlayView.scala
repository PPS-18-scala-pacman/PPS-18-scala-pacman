package it.unibo.scalapacman.client.gui

import java.awt.event.{ActionEvent, KeyEvent}
import java.awt.{BorderLayout, Color, Component, Font, GridLayout}

import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.utility.Action.{CHANGE_VIEW, KEY_PRESSED, KEY_RELEASED, MOVEMENT}
import it.unibo.scalapacman.client.controller.GameController
import it.unibo.scalapacman.client.utility.KeyTap
import it.unibo.scalapacman.client.utility.View.MENU
import javax.swing.{AbstractAction, ActionMap, BorderFactory, InputMap, JButton, JComponent, JLabel, JTextPane, KeyStroke, SwingConstants}

class PlayView extends PanelImpl with Logging {
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

  private val IFW = JComponent.WHEN_IN_FOCUSED_WINDOW
  private val UP = KeyEvent.VK_UP
  private val DOWN = KeyEvent.VK_DOWN
  private val RIGHT = KeyEvent.VK_RIGHT
  private val LEFT = KeyEvent.VK_LEFT
  private val UP_PRESSED = "up_pressed"
  private val UP_RELEASED = "up_released"
  private val DOWN_PRESSED = "down_pressed"
  private val DOWN_RELEASED = "down_released"
  private val RIGHT_PRESSED = "right_pressed"
  private val RIGHT_RELEASED = "right_released"
  private val LEFT_PRESSED = "left_pressed"
  private val LEFT_RELEASED = "left_released"

  private val livesCount: JLabel = createLabel(STARTING_LIVES_COUNT.toString)
  private val pointsCount: JLabel = createLabel(STARTING_POINTS_COUNT.toString)

  private val textPane: JTextPane = initTextPane
  private val placeholderLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val pointsLabel: JLabel = createLabel(POINTS_LABEL)
  private val livesLabel: JLabel = createLabel(LIVES_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  bindKeys(textPane)

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

  backButton addActionListener (_ => GameController.handleAction(CHANGE_VIEW, Some(MENU)))

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
//  playPanel add (placeholderLabel, BorderLayout.CENTER)
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
      setBackground(BACKGROUND_COLOR)
    }

    val doc = tp.getStyledDocument
    // Personalizza stile documento
    addStylesToDocument(doc)

    tp
  }

  private def bindKeys(component: JComponent): Unit = {
    val im: InputMap = component.getInputMap(IFW)
    val am: ActionMap = component.getActionMap

    im.clear()
    am.clear()

    im put (KeyStroke.getKeyStroke(UP, 0 , false), UP_PRESSED)
    im put (KeyStroke.getKeyStroke(UP, 0, true), UP_RELEASED)
    im put (KeyStroke.getKeyStroke(DOWN, 0 , false), DOWN_PRESSED)
    im put (KeyStroke.getKeyStroke(DOWN, 0 , true), DOWN_RELEASED)
    im put (KeyStroke.getKeyStroke(RIGHT, 0 , false), RIGHT_PRESSED)
    im put (KeyStroke.getKeyStroke(RIGHT, 0 , true), RIGHT_RELEASED)
    im put (KeyStroke.getKeyStroke(LEFT, 0 , false), LEFT_PRESSED)
    im put (KeyStroke.getKeyStroke(LEFT, 0 , true), LEFT_RELEASED)

    am put (UP_PRESSED, KeyAction(KeyEvent.getKeyText(UP), KEY_PRESSED))
    am put (UP_RELEASED, KeyAction(KeyEvent.getKeyText(UP), KEY_RELEASED))
    am put (DOWN_PRESSED, KeyAction(KeyEvent.getKeyText(DOWN), KEY_PRESSED))
    am put (DOWN_RELEASED, KeyAction(KeyEvent.getKeyText(DOWN), KEY_RELEASED))
    am put (RIGHT_PRESSED, KeyAction(KeyEvent.getKeyText(RIGHT), KEY_PRESSED))
    am put (RIGHT_RELEASED, KeyAction(KeyEvent.getKeyText(RIGHT), KEY_RELEASED))
    am put (LEFT_PRESSED, KeyAction(KeyEvent.getKeyText(LEFT), KEY_PRESSED))
    am put (LEFT_RELEASED, KeyAction(KeyEvent.getKeyText(LEFT), KEY_RELEASED))
  }

  case class KeyAction(key: String, action: KeyTap) extends AbstractAction {
    override def actionPerformed(ae: ActionEvent): Unit = action match {
      case KEY_RELEASED =>
        printDebug("rilasciato")
        notifyHandler()
      case KEY_PRESSED =>
        printDebug("premuto")
        notifyHandler()
      case _ => error("Tipologia di KeyTap sconosciuta")
    }

    private def printDebug(pressedOrReleased: String): Unit = debug(s"Il tasto $key è stato $pressedOrReleased")

    private def notifyHandler(): Unit = GameController.handleAction(action, Some(key))
  }
}
