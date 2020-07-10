package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, Component}

import it.unibo.scalapacman.client.utility.Action.CHANGE_VIEW
import it.unibo.scalapacman.client.controller.GameController
import it.unibo.scalapacman.client.utility.View.MENU
import javax.swing.{JButton, JLabel, SwingConstants}

class OptionsView extends PanelImpl {
  private val TITLE_LABEL: String = "Options View"
  private val BACK_BUTTON_LABEL: String = "Indietro"

  private val placeholderLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  placeholderLabel setHorizontalAlignment SwingConstants.CENTER

  backButton addActionListener (_ => GameController.handleAction(CHANGE_VIEW, Some(MENU)))

  private val buttonsPanel: PanelImpl = new PanelImpl

  backButton setAlignmentX Component.CENTER_ALIGNMENT

  buttonsPanel add backButton

  setLayout(new BorderLayout)
  add(placeholderLabel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)
}
