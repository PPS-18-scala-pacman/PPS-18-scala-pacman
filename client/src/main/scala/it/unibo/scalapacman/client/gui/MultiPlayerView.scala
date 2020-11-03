package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, GridBagLayout, GridLayout}

import it.unibo.scalapacman.client.controller.Action.START_GAME_MULTI
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.gui.View.MENU
import javax.swing.{BorderFactory, Box, BoxLayout, JButton, JLabel, JTextField, SwingConstants}

object MultiPlayerView {
  def apply()(implicit controller: Controller, viewChanger: ViewChanger): MultiPlayerView = new MultiPlayerView()
}

class MultiPlayerView(implicit controller: Controller, viewChanger: ViewChanger) extends PanelImpl {
  private val TITLE_LABEL: String = "Multigiocatore"
  private val PLAY_BUTTON_LABEL: String = "Gioca"
  private val BACK_BUTTON_LABEL: String = "Indietro"
  private val NUMBER_OF_PLAYERS: String = "Numero di giocatori"
  private val SL_ROWS: Int = 1
  private val SL_COLS: Int = 2
  private val SL_H_GAP: Int = 10
  private val SL_V_GAP: Int = 30
  private val SL_EMPTY_BORDER_Y_AXIS: Int = 275
  private val SL_EMPTY_BORDER_X_AXIS: Int = 100

  private val titleLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val playButton: JButton = createButton(PLAY_BUTTON_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  private val numPlayersLabel: JLabel = createLabel(NUMBER_OF_PLAYERS)

  private val numPlayersTextField: JTextField = createTextField()

  numPlayersTextField setText "2"

  titleLabel setHorizontalAlignment SwingConstants.CENTER

  playButton addActionListener (_ => handlePlayButton())
  backButton addActionListener (_ => viewChanger.changeView(MENU))

  private val settingsPanel: PanelImpl = PanelImpl()
  private val buttonsPanel: PanelImpl = PanelImpl()

  buttonsPanel add playButton
  buttonsPanel add backButton

  settingsPanel setLayout new GridLayout(SL_ROWS, SL_COLS, SL_H_GAP, SL_V_GAP)
  settingsPanel add numPlayersLabel
  settingsPanel add numPlayersTextField
  settingsPanel setBorder BorderFactory.createEmptyBorder(SL_EMPTY_BORDER_Y_AXIS, SL_EMPTY_BORDER_X_AXIS, SL_EMPTY_BORDER_Y_AXIS, SL_EMPTY_BORDER_X_AXIS)

  setLayout(new BorderLayout)
  add(titleLabel, BorderLayout.PAGE_START)
  add(settingsPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)

  private def handlePlayButton(): Unit = controller.handleAction(START_GAME_MULTI, Some(numPlayersTextField getText))
}
