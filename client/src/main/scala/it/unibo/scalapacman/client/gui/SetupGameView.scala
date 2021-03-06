package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, GridLayout}
import it.unibo.scalapacman.client.controller.Action.{CREATE_LOBBY, JOIN_LOBBY, SUBSCRIBE_TO_EVENTS}
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.event.{LobbiesUpdate, PacmanEvent, PacmanSubscriber}
import it.unibo.scalapacman.client.gui.View.{LOBBY, MENU}
import it.unibo.scalapacman.client.model.{CreateLobbyData, JoinLobbyData, Lobby}
import it.unibo.scalapacman.client.utils.UserDialog.showError

import javax.swing.{BorderFactory, Box, BoxLayout, DefaultListModel, JButton, JLabel, JScrollPane, JSeparator, JSpinner, JTextField, SwingConstants}

object SetupGameView {
  def apply()(implicit controller: Controller, viewChanger: ViewChanger): SetupGameView = new SetupGameView()
}

class SetupGameView(implicit controller: Controller, viewChanger: ViewChanger) extends PanelImpl with AskToController {
  private val TITLE_LABEL: String = "Setup partita"
  private val CREATE_GAME_BUTTON_LABEL: String = "Crea"
  private val JOIN_GAME_BUTTON_LABEL: String = "Partecipa"
  private val BACK_BUTTON_LABEL: String = "Indietro"
  private val NICKNAME_LABEL: String = "Nickname"
  private val NUMBER_OF_PLAYERS_LABEL: String = "Numero di giocatori"
  private val SUB_SL_ROWS: Int = 1
  private val SUB_SL_COLS: Int = 3
  private val SUB_SL_H_GAP: Int = 10
  private val SUB_SL_V_GAP: Int = 30
  private val SL_EMPTY_BORDER_Y_AXIS: Int = 100
  private val SUB_SL_EMPTY_BORDER_Y_AXIS: Int = 10
  private val SL_EMPTY_BORDER_X_AXIS: Int = 10
  private val SUB_SL_EMPTY_BORDER_X_AXIS: Int = 100
  private val BL_STRUCT_GAP: Int = 5
  private val MIN_PLAYERS: Int = 1
  private val MAX_PLAYERS: Int = 4

  private val titleLabel: JLabel = createTitleLabel(TITLE_LABEL)
  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  private val emptyLabel: JLabel = createLabel("")
  private val nicknameLabel: JLabel = createLabel(NICKNAME_LABEL)
  private val numPlayersLabel: JLabel = createLabel(NUMBER_OF_PLAYERS_LABEL)

  private val nicknameTextField: JTextField = createTextField()
  private val numPlayersSpinner: JSpinner = createNumberPlayersField()
  private val gameIdTextField: JTextField = createTextField()

  nicknameTextField setHorizontalAlignment SwingConstants.RIGHT
  nicknameTextField setText controller.model.username

  gameIdTextField setHorizontalAlignment SwingConstants.RIGHT

  private val createGameButton: JButton = createButton(CREATE_GAME_BUTTON_LABEL)
  private val joinGameButton: JButton = createButton(JOIN_GAME_BUTTON_LABEL)

  titleLabel setHorizontalAlignment SwingConstants.CENTER

  createGameButton addActionListener (_ => handleCreateGameButton())
  joinGameButton addActionListener (_ => handleJoinGameButton())
  backButton addActionListener (_ => viewChanger.changeView(MENU))

  private val settingsPanel: PanelImpl = PanelImpl()
  private val buttonsPanel: PanelImpl = PanelImpl()

  private val nicknamePanel: PanelImpl = PanelImpl()
  private val newGamePanel: PanelImpl = PanelImpl()
  private val joinGamePanel: PanelImpl = PanelImpl()

  buttonsPanel add backButton

  nicknamePanel setLayout new GridLayout(SUB_SL_ROWS, SUB_SL_COLS, SUB_SL_H_GAP, SUB_SL_V_GAP)
  nicknamePanel add nicknameLabel
  nicknamePanel add nicknameTextField
  nicknamePanel add emptyLabel // Per lasciare "vuoto" lo spazio
  nicknamePanel setBorder BorderFactory.createEmptyBorder(
    SUB_SL_EMPTY_BORDER_Y_AXIS,
    SUB_SL_EMPTY_BORDER_X_AXIS,
    SUB_SL_EMPTY_BORDER_Y_AXIS,
    SUB_SL_EMPTY_BORDER_X_AXIS
  )

  newGamePanel setLayout new GridLayout(SUB_SL_ROWS, SUB_SL_COLS, SUB_SL_H_GAP, SUB_SL_V_GAP)
  newGamePanel add numPlayersLabel
  newGamePanel add numPlayersSpinner
  newGamePanel add createGameButton
  newGamePanel setBorder BorderFactory.createEmptyBorder(
    SUB_SL_EMPTY_BORDER_Y_AXIS,
    SUB_SL_EMPTY_BORDER_X_AXIS,
    SUB_SL_EMPTY_BORDER_Y_AXIS,
    SUB_SL_EMPTY_BORDER_X_AXIS
  )

  private val lobbyList = new DefaultListModel[Lobby]()
  private val lobbyJList = createJList(lobbyList)
  private val listScrollPane = new JScrollPane(lobbyJList)

  joinGamePanel setLayout new BorderLayout(0, SUB_SL_V_GAP)
  joinGamePanel add(listScrollPane, BorderLayout.CENTER)
  joinGamePanel add(joinGameButton, BorderLayout.PAGE_END)
  joinGamePanel setBorder BorderFactory.createEmptyBorder(
    SUB_SL_EMPTY_BORDER_Y_AXIS,
    SUB_SL_EMPTY_BORDER_X_AXIS,
    SUB_SL_EMPTY_BORDER_Y_AXIS,
    SUB_SL_EMPTY_BORDER_X_AXIS
  )

  settingsPanel setLayout new BoxLayout(settingsPanel, BoxLayout.PAGE_AXIS)
  settingsPanel add nicknamePanel
  settingsPanel add Box.createVerticalStrut(BL_STRUCT_GAP)
  settingsPanel add new JSeparator(SwingConstants.HORIZONTAL)
  settingsPanel add Box.createVerticalStrut(BL_STRUCT_GAP)
  settingsPanel add newGamePanel
  settingsPanel add Box.createVerticalStrut(BL_STRUCT_GAP)
  settingsPanel add new JSeparator(SwingConstants.HORIZONTAL)
  settingsPanel add Box.createVerticalStrut(BL_STRUCT_GAP)
  settingsPanel add joinGamePanel
  settingsPanel setBorder BorderFactory.createEmptyBorder(SL_EMPTY_BORDER_Y_AXIS, SL_EMPTY_BORDER_X_AXIS, SL_EMPTY_BORDER_Y_AXIS, SL_EMPTY_BORDER_X_AXIS)

  setLayout(new BorderLayout)
  add(titleLabel, BorderLayout.PAGE_START)
  add(settingsPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)

  askToController(SUBSCRIBE_TO_EVENTS, Some(PacmanSubscriber(handlePacmanEvent)))

  private def handleCreateGameButton(): Unit = if (checkNickName()) {
    askToController(CREATE_LOBBY, Some(CreateLobbyData(nicknameTextField.getText(), numPlayersSpinner.getValue.toString.toInt)))
    viewChanger.changeView(LOBBY)
  } else {
    showError("Devi prima impostare un nome utente")
  }

  private def handleJoinGameButton(): Unit = if (checkNickName()) {
    if (!lobbyJList.isSelectionEmpty) {
      val lobby: Lobby = lobbyJList.getSelectedValue

      askToController(JOIN_LOBBY, Some(JoinLobbyData(nicknameTextField.getText(), lobby)))
      lobbyJList.clearSelection()

      viewChanger.changeView(LOBBY)
    } else {
      showError("Devi prima selezionare una lobby")
    }
  } else {
    showError("Devi prima impostare un nome utente")
  }

  private def createNumberPlayersField(): JSpinner = createNumericJSpinner(MIN_PLAYERS, MIN_PLAYERS, MAX_PLAYERS)

  private def checkNickName(): Boolean = !nicknameTextField.getText().equals("")

  private def updateLobbies(lobbyList: DefaultListModel[Lobby], lobbies: List[Lobby]): Unit = {
    lobbyList clear()
    lobbies foreach { lobbyList.addElement }
  }

  private def handlePacmanEvent(pe: PacmanEvent): Unit = pe match {
    case LobbiesUpdate(lobbies) => updateLobbies(lobbyList, lobbies)
    case _ => Unit
  }
}
