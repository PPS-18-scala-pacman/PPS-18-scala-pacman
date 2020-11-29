package it.unibo.scalapacman.client.gui

import java.awt.BorderLayout

import it.unibo.scalapacman.client.controller.Action.{LEAVE_LOBBY, SUBSCRIBE_TO_EVENTS}
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.event.{GameStarted, LobbyDeleted, LobbyUpdate, PacmanEvent, PacmanSubscriber}
import it.unibo.scalapacman.client.gui.View.{PLAY, SETUP}
import it.unibo.scalapacman.client.model.{Lobby, Participant}
import javax.swing.{BorderFactory, DefaultListModel, JButton, JLabel, JScrollPane, SwingConstants}

object LobbyView {
  def apply()(implicit controller: Controller, viewChanger: ViewChanger): LobbyView = new LobbyView()
}

class LobbyView(implicit controller: Controller, viewChanger: ViewChanger) extends PanelImpl with AskToController {
  private val TITLE_LABEL: String = "Lista partecipanti"
  private val LEAVE_BUTTON_LABEL: String = "Abbandona"
  private val SUB_SL_EMPTY_BORDER_Y_AXIS: Int = 10
  private val SUB_SL_EMPTY_BORDER_X_AXIS: Int = 100

  private val lobbyDescriptionLabel: JLabel = createTitleLabel("")

  lobbyDescriptionLabel setHorizontalAlignment SwingConstants.CENTER

  private val leaveButton: JButton = createButton(LEAVE_BUTTON_LABEL)

  leaveButton addActionListener (_ => handleLeaveButton())

  private val buttonsPanel: PanelImpl = PanelImpl()

  buttonsPanel add leaveButton

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
    lobbyDescriptionLabel setText lobby.description
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
}
