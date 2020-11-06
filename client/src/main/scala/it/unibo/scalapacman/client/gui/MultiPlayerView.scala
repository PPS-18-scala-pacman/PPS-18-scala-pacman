package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, GridBagLayout, GridLayout}

import it.unibo.scalapacman.client.controller.Action.{JOIN_GAME_MULTI, START_GAME_MULTI}
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.gui.View.MENU
import it.unibo.scalapacman.client.model.{CreateGameData, JoinGameData}
import javax.swing.{BorderFactory, Box, BoxLayout, JButton, JLabel, JSpinner, JTextField, SwingConstants}

object MultiPlayerView {
  def apply()(implicit controller: Controller, viewChanger: ViewChanger): MultiPlayerView = new MultiPlayerView()
}

class MultiPlayerView(implicit controller: Controller, viewChanger: ViewChanger) extends PanelImpl {
  private val TITLE_LABEL: String = "Multigiocatore"
  private val CREATE_GAME_BUTTON_LABEL: String = "Crea"
  private val JOIN_GAME_BUTTON_LABEL: String = "Partecipa"
  private val BACK_BUTTON_LABEL: String = "Indietro"
  private val NICKNAME_LABEL: String = "Nickname"
  private val NUMBER_OF_PLAYERS_LABEL: String = "Numero di giocatori"
  private val GAME_ID_LABEL: String = "ID Partita"
  private val SL_ROWS: Int = 3
  private val SL_COLS: Int = 3
  private val SL_H_GAP: Int = 10
  private val SL_V_GAP: Int = 30
  private val SL_EMPTY_BORDER_Y_AXIS: Int = 345
  private val SL_EMPTY_BORDER_X_AXIS: Int = 100
  private val MIN_PLAYERS: Int = 2
  private val MAX_PLAYERS: Int = 4

  private val titleLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  private val emptyLabel: JLabel = createLabel("")
  private val nicknameLabel: JLabel = createLabel(NICKNAME_LABEL)
  private val numPlayersLabel: JLabel = createLabel(NUMBER_OF_PLAYERS_LABEL)
  private val joinGameLabel: JLabel = createLabel(GAME_ID_LABEL)

  private val nicknameTextField: JTextField = createTextField()
  private val numPlayersSpinner: JSpinner = createNumberPlayersField()
  private val gameIdTextField: JTextField = createTextField()

  private val createGameButton: JButton = createButton(CREATE_GAME_BUTTON_LABEL)
  private val joinGameButton: JButton = createButton(JOIN_GAME_BUTTON_LABEL)

  titleLabel setHorizontalAlignment SwingConstants.CENTER

  createGameButton addActionListener (_ => handleCreateGameButton())
  joinGameButton addActionListener (_ => handleJoinGameButton())
  backButton addActionListener (_ => viewChanger.changeView(MENU))

  private val settingsPanel: PanelImpl = PanelImpl()
  private val buttonsPanel: PanelImpl = PanelImpl()

  buttonsPanel add backButton

  settingsPanel setLayout new GridLayout(SL_ROWS, SL_COLS, SL_H_GAP, SL_V_GAP)
  settingsPanel add nicknameLabel
  settingsPanel add nicknameTextField
  settingsPanel add emptyLabel // Per lasciare "vuoto" lo spazio
  settingsPanel add numPlayersLabel
  settingsPanel add numPlayersSpinner
  settingsPanel add createGameButton
  settingsPanel add joinGameLabel
  settingsPanel add gameIdTextField
  settingsPanel add joinGameButton
  settingsPanel setBorder BorderFactory.createEmptyBorder(SL_EMPTY_BORDER_Y_AXIS, SL_EMPTY_BORDER_X_AXIS, SL_EMPTY_BORDER_Y_AXIS, SL_EMPTY_BORDER_X_AXIS)

  setLayout(new BorderLayout)
  add(titleLabel, BorderLayout.PAGE_START)
  add(settingsPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)

  // TODO: dovrebbe redirezionare alla schermata di gioco
  private def handleCreateGameButton(): Unit = if (checkNickName()) {
    controller.handleAction(START_GAME_MULTI, Some(CreateGameData(nicknameTextField.getText(), numPlayersSpinner.getValue.toString.toInt)))
    viewChanger.changeView(View.PLAY)
  }

  private def handleJoinGameButton(): Unit = if (checkNickName()) {
    controller.handleAction(JOIN_GAME_MULTI, Some(JoinGameData(nicknameTextField.getText(), gameIdTextField.getText)))
//    viewChanger.changeView(View.LOBBY)
  }

  private def createNumberPlayersField(): JSpinner = createNumericJSpinner(MIN_PLAYERS, MIN_PLAYERS, MAX_PLAYERS)

  private def checkNickName(): Boolean = !nicknameTextField.getText().equals("")
}
