package it.unibo.scalapacman.client.gui

import java.awt.BorderLayout

import it.unibo.scalapacman.client.controller.Action.{LEAVE_LOBBY, START_LOBBY_GAME, SUBSCRIBE_TO_EVENTS}
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.event.{GameStarted, LobbyDeleted, LobbyUpdate, PacmanEvent, PacmanSubscriber}
import it.unibo.scalapacman.client.gui.View.{PLAY, SETUP}
import it.unibo.scalapacman.client.model.{Lobby, Participant}
import javax.swing.{BorderFactory, DefaultListModel, JButton, JLabel, JScrollPane, SwingConstants}

object LobbyView {
  def apply()(implicit controller: Controller, viewChanger: ViewChanger): LobbyView = new LobbyView()
}

class LobbyView(implicit controller: Controller, viewChanger: ViewChanger) extends PanelImpl with AskToController {
  private val START_LOBBY_GAME_BUTTON_LABEL: String = "Avvia partita"
  private val LEAVE_LOBBY_BUTTON_LABEL: String = "Abbandona"
  private val SUB_SL_EMPTY_BORDER_Y_AXIS: Int = 10
  private val SUB_SL_EMPTY_BORDER_X_AXIS: Int = 100

  private val lobbyDescriptionLabel: JLabel = createTitleLabel("")

  lobbyDescriptionLabel setHorizontalAlignment SwingConstants.CENTER

  private val startLobbyGameButton: JButton = createButton(START_LOBBY_GAME_BUTTON_LABEL)
  private val leaveLobbyButton: JButton = createButton(LEAVE_LOBBY_BUTTON_LABEL)

  startLobbyGameButton addActionListener (_ => handleStartGameButton())
  leaveLobbyButton addActionListener (_ => handleLeaveButton())

  private val buttonsPanel: PanelImpl = PanelImpl()

  startLobbyGameButton setEnabled false

  buttonsPanel add startLobbyGameButton
  buttonsPanel add leaveLobbyButton

  private val playersPanel: PanelImpl = PanelImpl()

  private val playersList = new DefaultListModel[Participant]()
  private val playersJList = createJList(playersList)
  private val listScrollPane = new JScrollPane(playersJList)

  playersPanel setLayout new BorderLayout(0, 0)
  playersPanel add(listScrollPane, BorderLayout.CENTER)
  playersPanel setBorder BorderFactory.createEmptyBorder(
    SUB_SL_EMPTY_BORDER_Y_AXIS,
    SUB_SL_EMPTY_BORDER_X_AXIS,
    SUB_SL_EMPTY_BORDER_Y_AXIS,
    SUB_SL_EMPTY_BORDER_X_AXIS
  )

  setLayout(new BorderLayout)
  add(lobbyDescriptionLabel, BorderLayout.PAGE_START)
  add(playersPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)

  askToController(SUBSCRIBE_TO_EVENTS, Some(PacmanSubscriber(handlePacmanEvent)))

  private def updateLobby(lobby: Lobby): Unit = {
    if (lobby.hostUsername.equals(controller.model.username) && !startLobbyGameButton.isEnabled) {
      startLobbyGameButton setEnabled true
    } else if (!lobby.hostUsername.equals(controller.model.username) && startLobbyGameButton.isEnabled) {
      startLobbyGameButton setEnabled false
    }

    lobbyDescriptionLabel setText s"${lobby.description} - (${lobby.participants.size}/${lobby.size})"
    playersList clear()
    lobby.participants foreach { playersList.addElement }
  }

  private def handlePacmanEvent(pe: PacmanEvent): Unit = pe match {
    case LobbyUpdate(lobby) => updateLobby(lobby)
    case LobbyDeleted() => viewChanger.changeView(SETUP)
    case GameStarted() => viewChanger.changeView(PLAY)
    case _ => Unit
  }

  private def handleLeaveButton(): Unit = {
    askToController(LEAVE_LOBBY, None)
    viewChanger.changeView(SETUP)
  }

  private def handleStartGameButton(): Unit = if (controller.model.lobby.get.hostUsername.equals(controller.model.username)) {
    askToController(START_LOBBY_GAME, None)
  }
}
