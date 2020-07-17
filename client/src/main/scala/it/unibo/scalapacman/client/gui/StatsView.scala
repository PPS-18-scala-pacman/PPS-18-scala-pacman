package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, Component}

import it.unibo.scalapacman.client.controller.Action.CHANGE_VIEW
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.gui.View.MENU
import javax.swing.{JButton, JLabel, SwingConstants}

object StatsView {
  def apply()(implicit controller: Controller): StatsView = new StatsView()
}

class StatsView(implicit controller: Controller) extends PanelImpl {
  private val TITLE_LABEL: String = "Stats View"
  private val BACK_BUTTON_LABEL: String = "Indietro"

  private val placeholderLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  placeholderLabel setHorizontalAlignment SwingConstants.CENTER

  backButton addActionListener (_ => controller.handleAction(CHANGE_VIEW, Some(MENU)))

  private val buttonsPanel: PanelImpl = PanelImpl()

  buttonsPanel add backButton

  setLayout(new BorderLayout)
  add(placeholderLabel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)
}
