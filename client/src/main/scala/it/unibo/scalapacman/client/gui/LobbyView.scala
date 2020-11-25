package it.unibo.scalapacman.client.gui

import java.awt.BorderLayout

import it.unibo.scalapacman.client.controller.Action.SUBSCRIBE_TO_EVENTS
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.event.{LobbyUpdate, PacmanEvent, PacmanSubscriber}
import it.unibo.scalapacman.client.gui.View.SETUP
import it.unibo.scalapacman.client.model.{Lobby, Participant}
import javax.swing.{BorderFactory, DefaultListModel, JButton, JScrollPane}

object LobbyView {
  def apply()(implicit controller: Controller, viewChanger: ViewChanger): LobbyView = new LobbyView()
}

class LobbyView(implicit controller: Controller, viewChanger: ViewChanger) extends PanelImpl with AskToController {
  private val BACK_BUTTON_LABEL: String = "Indietro"
  private val SUB_SL_EMPTY_BORDER_Y_AXIS: Int = 10
  private val SUB_SL_EMPTY_BORDER_X_AXIS: Int = 100

  private val backButton: JButton = createButton(BACK_BUTTON_LABEL)

  backButton addActionListener (_ => viewChanger.changeView(SETUP))

  private val buttonsPanel: PanelImpl = PanelImpl()

  buttonsPanel add backButton

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
  add(playersPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.PAGE_END)

  askToController(SUBSCRIBE_TO_EVENTS, Some(PacmanSubscriber(handlePacmanEvent)))

  private def updateLobby(lobby: Lobby): Unit = {
    playersList clear()
    lobby.participants foreach { playersList.addElement }
  }

  private def handlePacmanEvent(pe: PacmanEvent): Unit = pe match {
    case LobbyUpdate(lobby) => updateLobby(lobby)
    case _ => Unit
  }
}
