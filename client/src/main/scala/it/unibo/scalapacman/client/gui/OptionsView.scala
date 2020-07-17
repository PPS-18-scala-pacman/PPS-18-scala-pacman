package it.unibo.scalapacman.client.gui

import java.awt.event.{KeyEvent, KeyListener}
import java.awt.{BorderLayout, GridLayout}

import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.controller.Action.CHANGE_VIEW
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.gui.View.MENU
import it.unibo.scalapacman.client.input.{KeyBinder, KeyMap}
import javax.swing.{BorderFactory, JButton, JLabel, JTextField, SwingConstants}

import scala.collection.mutable

object OptionsView {
  def apply(keyBinder: KeyBinder)(implicit controller: Controller): OptionsView = new OptionsView(keyBinder)
}

class OptionsView(keyBinder: KeyBinder)(implicit controller: Controller) extends PanelImpl with Logging {
  private val TITLE_LABEL: String = "Imposta tasti"
  private val SAVE_BUTTON_LABEL: String = "Salva"
  private val BACK_BUTTON_LABEL: String = "Indietro"
  private val UP_LABEL: String = "Su"
  private val DOWN_LABEL: String = "Giù"
  private val RIGHT_LABEL: String = "Destra"
  private val LEFT_LABEL: String = "Sinistra"
  private val KBL_ROWS: Int = 4
  private val KBL_COLS: Int = 2
  private val KBL_H_GAP: Int = 10
  private val KBL_V_GAP: Int = 30
  private val KBL_EMPTY_BORDER_Y_AXIS: Int = 275
  private val KBL_EMPTY_BORDER_X_AXIS: Int = 100

  private val titleLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val saveButton: JButton = createButton(SAVE_BUTTON_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  private val upLabel: JLabel = createLabel(UP_LABEL)
  private val downLabel: JLabel = createLabel(DOWN_LABEL)
  private val rightLabel: JLabel = createLabel(RIGHT_LABEL)
  private val leftLabel: JLabel = createLabel(LEFT_LABEL)

  private val upTextField: JTextField = createTextField()
  private val downTextField: JTextField = createTextField()
  private val rightTextField: JTextField = createTextField()
  private val leftTextField: JTextField = createTextField()

  private val upIdentifier: String = "UP"
  private val downIdentifier: String = "DOWN"
  private val rightIdentifier: String = "RIGHT"
  private val leftIdentifier: String = "LEFT"

  private val keyMapMap: mutable.Map[String, Int] = mutable.Map(
    upIdentifier -> controller.getKeyMap.up,
    downIdentifier -> controller.getKeyMap.down,
    rightIdentifier -> controller.getKeyMap.right,
    leftIdentifier -> controller.getKeyMap.left,
  )

  titleLabel setHorizontalAlignment SwingConstants.CENTER

  backButton addActionListener (_ => controller.handleAction(CHANGE_VIEW, Some(MENU)))
  saveButton addActionListener (_ =>
    saveKeyConfiguration(keyBinder, KeyMap(keyMapMap(upIdentifier), keyMapMap(downIdentifier), keyMapMap(rightIdentifier), keyMapMap(leftIdentifier)))
  )

  upLabel setLabelFor upTextField
  downLabel setLabelFor downTextField
  leftLabel setLabelFor rightTextField
  leftLabel setLabelFor leftTextField

  updateTextField(upTextField)(controller.getKeyMap.up)
  updateTextField(downTextField)(controller.getKeyMap.down)
  updateTextField(rightTextField)(controller.getKeyMap.right)
  updateTextField(leftTextField)(controller.getKeyMap.left)

  upTextField addKeyListener setKeyTextFieldKeyListener(updateTextField(upTextField), updateKeyCode(keyMapMap, upIdentifier))
  downTextField addKeyListener setKeyTextFieldKeyListener(updateTextField(downTextField), updateKeyCode(keyMapMap, downIdentifier))
  rightTextField addKeyListener setKeyTextFieldKeyListener(updateTextField(rightTextField), updateKeyCode(keyMapMap, rightIdentifier))
  leftTextField addKeyListener setKeyTextFieldKeyListener(updateTextField(leftTextField), updateKeyCode(keyMapMap, leftIdentifier))

  private val titlePanel: PanelImpl = PanelImpl()
  private val buttonsPanel: PanelImpl = PanelImpl()
  private val keyBindingPanel: PanelImpl = PanelImpl()

  titlePanel add titleLabel

  buttonsPanel add saveButton
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

  keyBindingPanel setBorder BorderFactory.createEmptyBorder(KBL_EMPTY_BORDER_Y_AXIS, KBL_EMPTY_BORDER_X_AXIS, KBL_EMPTY_BORDER_Y_AXIS, KBL_EMPTY_BORDER_X_AXIS)

  setLayout(new BorderLayout)
  add(titlePanel, BorderLayout.PAGE_START)
  add(keyBindingPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)

  private def setKeyTextFieldKeyListener(updateTextField: Int => Unit, updateKeyCode: Int => Unit): KeyListener = new KeyListener {
    override def keyTyped(keyEvent: KeyEvent): Unit = Unit

    override def keyPressed(keyEvent: KeyEvent): Unit = Unit

    override def keyReleased(keyEvent: KeyEvent): Unit = {
      updateTextField(keyEvent.getKeyCode)
      updateKeyCode(keyEvent.getKeyCode)
    }
  }

  private def updateTextField(keyTextField: JTextField)(keyCode: Int): Unit = keyTextField setText KeyEvent.getKeyText(keyCode)

  private def updateKeyCode(keyCodes: mutable.Map[String, Int], keyIdentifier: String)(keyCode: Int): Unit = keyCodes(keyIdentifier) = keyCode

  private def saveKeyConfiguration(keyBinder: KeyBinder, keyMap: KeyMap): Unit = keyBinder applyKeyBinding keyMap
}
