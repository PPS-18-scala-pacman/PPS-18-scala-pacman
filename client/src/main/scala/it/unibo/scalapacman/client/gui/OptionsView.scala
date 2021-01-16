package it.unibo.scalapacman.client.gui

import java.awt.event.{KeyEvent, KeyListener}
import java.awt.{BorderLayout, GridLayout}

import it.unibo.scalapacman.client.controller.Action.{RESET_KEY_MAP, SAVE_KEY_MAP}
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.gui.View.MENU
import it.unibo.scalapacman.client.input.KeyMap
import javax.swing.{BorderFactory, JButton, JLabel, JTextField, SwingConstants}

object OptionsView {
  def apply()(implicit controller: Controller, viewChanger: ViewChanger): OptionsView = new OptionsView()
}

/**
 * Schermata di configurazione dei comandi di gioco, da qui l'utente può modificare i tasti utilizzati
 * per controllare Pacman durante il gioco e il tasto per richiedere la pausa/ripresa del gioco.
 *
 * @param controller il riferimento al componente Controller
 * @param viewChanger il riferimento al componente che gestisce il cambio schermata
 */
class OptionsView()(implicit controller: Controller, viewChanger: ViewChanger) extends PanelImpl with AskToController {
  private val TITLE_LABEL: String = "Imposta tasti"
  private val SAVE_BUTTON_LABEL: String = "Salva"
  private val RESET_BUTTON_LABEL: String = "Reimposta"
  private val BACK_BUTTON_LABEL: String = "Indietro"
  private val UP_LABEL: String = "Su"
  private val DOWN_LABEL: String = "Giù"
  private val RIGHT_LABEL: String = "Destra"
  private val LEFT_LABEL: String = "Sinistra"
  private val PAUSE_LABEL: String = "Pausa"
  private val KBL_ROWS: Int = 5
  private val KBL_COLS: Int = 2
  private val KBL_H_GAP: Int = 10
  private val KBL_V_GAP: Int = 30
  private val KBL_EMPTY_BORDER_Y_AXIS: Int = 275
  private val KBL_EMPTY_BORDER_X_AXIS: Int = 100

  private val titleLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val saveButton: JButton = createButton(SAVE_BUTTON_LABEL)
  private val resetButton: JButton = createButton(RESET_BUTTON_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  private val upLabel: JLabel = createLabel(UP_LABEL)
  private val downLabel: JLabel = createLabel(DOWN_LABEL)
  private val rightLabel: JLabel = createLabel(RIGHT_LABEL)
  private val leftLabel: JLabel = createLabel(LEFT_LABEL)
  private val pauseLabel: JLabel = createLabel(PAUSE_LABEL)

  private val upTextField: JTextField = createTextField()
  private val downTextField: JTextField = createTextField()
  private val rightTextField: JTextField = createTextField()
  private val leftTextField: JTextField = createTextField()
  private val pauseTextField: JTextField = createTextField()

  alignTextField()

  private val upIdentifier: String = "UP"
  private val downIdentifier: String = "DOWN"
  private val rightIdentifier: String = "RIGHT"
  private val leftIdentifier: String = "LEFT"
  private val pauseIdentifier: String = "PAUSE"

  private var keyMapMap: Map[String, Int] = createKeyMapMap(controller.model.keyMap)

  titleLabel setHorizontalAlignment SwingConstants.CENTER

  backButton addActionListener (_ => goBack())
  resetButton addActionListener (_ => resetKeyMap())
  saveButton addActionListener (_ =>
    saveKeyMap(KeyMap(keyMapMap(upIdentifier), keyMapMap(downIdentifier), keyMapMap(rightIdentifier), keyMapMap(leftIdentifier), keyMapMap(pauseIdentifier)))
  )

  upLabel setLabelFor upTextField
  downLabel setLabelFor downTextField
  rightLabel setLabelFor rightTextField
  leftLabel setLabelFor leftTextField
  pauseLabel setLabelFor pauseTextField

  resetTextFields()

  upTextField addKeyListener setKeyTextFieldKeyListener(updateTextField(upTextField), updateKeyMapMap(upIdentifier))
  downTextField addKeyListener setKeyTextFieldKeyListener(updateTextField(downTextField), updateKeyMapMap(downIdentifier))
  rightTextField addKeyListener setKeyTextFieldKeyListener(updateTextField(rightTextField), updateKeyMapMap(rightIdentifier))
  leftTextField addKeyListener setKeyTextFieldKeyListener(updateTextField(leftTextField), updateKeyMapMap(leftIdentifier))
  pauseTextField addKeyListener setKeyTextFieldKeyListener(updateTextField(pauseTextField), updateKeyMapMap(pauseIdentifier))

  private val titlePanel: PanelImpl = PanelImpl()
  private val buttonsPanel: PanelImpl = PanelImpl()
  private val keyBindingPanel: PanelImpl = PanelImpl()

  titlePanel add titleLabel

  buttonsPanel add saveButton
  buttonsPanel add resetButton
  buttonsPanel add backButton

  keyBindingPanel setLayout new GridLayout(KBL_ROWS, KBL_COLS, KBL_H_GAP, KBL_V_GAP)
  keyBindingPanel add upLabel
  keyBindingPanel add upTextField
  keyBindingPanel add downLabel
  keyBindingPanel add downTextField
  keyBindingPanel add rightLabel
  keyBindingPanel add rightTextField
  keyBindingPanel add leftLabel
  keyBindingPanel add leftTextField
  keyBindingPanel add pauseLabel
  keyBindingPanel add pauseTextField

  keyBindingPanel setBorder BorderFactory.createEmptyBorder(KBL_EMPTY_BORDER_Y_AXIS, KBL_EMPTY_BORDER_X_AXIS, KBL_EMPTY_BORDER_Y_AXIS, KBL_EMPTY_BORDER_X_AXIS)

  setLayout(new BorderLayout)
  add(titlePanel, BorderLayout.PAGE_START)
  add(keyBindingPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)

  /**
   * Definisce il comportamento delle JTextField quando un tasto viene premuto dentro ad esse
   *
   * @param updateTextField funzione per aggiornare la JTextField con la rappresentazione testuale del tasto
   * @param updateKeyCode funzione per aggiornare la mappa delle modifiche della configurazione dei tasti
   * @return
   */
  private def setKeyTextFieldKeyListener(updateTextField: Int => Unit, updateKeyCode: Int => Unit): KeyListener = new KeyListener {
    override def keyTyped(keyEvent: KeyEvent): Unit = Unit

    override def keyPressed(keyEvent: KeyEvent): Unit = Unit

    override def keyReleased(keyEvent: KeyEvent): Unit = {
      updateTextField(keyEvent.getKeyCode)
      updateKeyCode(keyEvent.getKeyCode)
    }
  }

  /**
   * Aggiorna le JTextField con l'attuale configurazione dei tasti
   *
   * @param keyMap l'attuale configurazione dei tasti
   */
  private def updateTextFields(keyMap: KeyMap): Unit = {
    updateTextField(upTextField)(keyMap.up)
    updateTextField(downTextField)(keyMap.down)
    updateTextField(rightTextField)(keyMap.right)
    updateTextField(leftTextField)(keyMap.left)
    updateTextField(pauseTextField)(keyMap.pause)
  }

  /**
   * Aggiorna il testo della JTextField con la rappresentazione testuale del tasto
   *
   * @param keyTextField la JTextField da aggiornare
   * @param keyCode codice del nuovo tasto impostato
   */
  private def updateTextField(keyTextField: JTextField)(keyCode: Int): Unit = keyTextField setText KeyEvent.getKeyText(keyCode)

  private def alignTextField(): Unit = {
    upTextField setHorizontalAlignment SwingConstants.RIGHT
    downTextField setHorizontalAlignment SwingConstants.RIGHT
    rightTextField setHorizontalAlignment SwingConstants.RIGHT
    leftTextField setHorizontalAlignment SwingConstants.RIGHT
    pauseTextField setHorizontalAlignment SwingConstants.RIGHT
  }

  /**
   * Aggiorna la mappa di configurazione dei comandi
   *
   * @param keyIdentifier il comando da aggiornare
   * @param keyCode codice del nuovo tasto impostato
   */
  private def updateKeyMapMap(keyIdentifier: String)(keyCode: Int): Unit = keyMapMap = keyMapMap + (keyIdentifier -> keyCode)

  /**
   * Informa il controller dell'intenzione dell'utente di voler salvare una nuova configurazione dei tasti
   *
   * @param keyMap la nuova configurazione dei tasti
   */
  private def saveKeyMap(keyMap: KeyMap): Unit = {
    askToController(SAVE_KEY_MAP, Some(keyMap))
    resetTextFields()
  }

  /**
   * Genera la mappa locale usata per tracciare le modifiche della configurazione dei tasti
   *
   * @param keyMap l'attuale configurazione dei tasti
   * @return la mappa generata
   */
  private def createKeyMapMap(keyMap: KeyMap): Map[String, Int] = Map(
    upIdentifier -> keyMap.up,
    downIdentifier -> keyMap.down,
    rightIdentifier -> keyMap.right,
    leftIdentifier -> keyMap.left,
    pauseIdentifier -> keyMap.pause,
  )

  /** Torna al menù */
  private def goBack(): Unit = {
    resetTextFields()
    viewChanger.changeView(MENU)
  }

  /**
   * Informa il controller dell'intenzione dell'utente di voler riportare
   * la configurazione dei tasti al valore di default
   */
  private def resetKeyMap(): Unit = {
    askToController(RESET_KEY_MAP, None)
    resetTextFields()
  }

  /**
   * Reimposta le JTextField con il valore di default della configurazione dei tasti
   */
  private def resetTextFields(): Unit = updateTextFields(controller.model.keyMap)
}
