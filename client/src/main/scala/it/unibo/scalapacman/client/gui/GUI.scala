package it.unibo.scalapacman.client.gui

import java.awt.event.{WindowAdapter, WindowEvent}
import java.awt.{BorderLayout, CardLayout, Dimension}
import it.unibo.scalapacman.client.controller.Action.EXIT_APP
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.gui.View.{LOBBY, MENU, OPTIONS, PLAY, SETUP, STATS}
import it.unibo.scalapacman.client.utils.UserDialog.showChoice

import javax.swing.{JFrame, JOptionPane, JPanel, WindowConstants}

object GUI {
  def apply(implicit controller: Controller): GUIImpl = new GUIImpl()
}

/**
 * Entry point della View.
 * Istanzia le schermate che compongono l'applicazione ed implementa il metodo per gestire il cambio schermata.
 *
 * @param controller il riferimento al componente Controller
 */
class GUIImpl(implicit val controller: Controller) extends ViewChanger with AskToController {

  implicit val viewChanger: ViewChanger = this

  private val frame: JFrame = new JFrame

  private val mainLayout: CardLayout = new CardLayout
  private val mainPanel = new JPanel(mainLayout)

  private val menuView: MenuView = MenuView()
  private val playView: PlayView = PlayView()
  private val optionsView: OptionsView = OptionsView()
  private val statsView: StatsView = StatsView()
  private val setupGameView: SetupGameView = SetupGameView()
  private val lobbyView: LobbyView = LobbyView()

  mainPanel add(menuView, MENU.name)
  mainPanel add(playView, PLAY.name)
  mainPanel add(setupGameView, SETUP.name)
  mainPanel add(lobbyView, LOBBY.name)
  mainPanel add(optionsView, OPTIONS.name)
  mainPanel add(statsView, STATS.name)

  changeView(MENU)

  setOnWindowClosing(frame)

  frame add(mainPanel, BorderLayout.CENTER)

  frame setTitle "Scala Pacman"
  frame setSize new Dimension(WIDTH, HEIGHT)
  frame setResizable false
  frame setDefaultCloseOperation WindowConstants.DO_NOTHING_ON_CLOSE
  frame setLocationRelativeTo null // scalastyle:ignore null
  frame setVisible true

  def changeView(view: View): Unit = {
    if (view == PLAY) playView.setupView()
    mainLayout show(mainPanel, view.name)
  }

  private def setOnWindowClosing(frame: JFrame): Unit = frame addWindowListener new WindowAdapter() {
    override def windowClosing(windowEvent: WindowEvent): Unit = {
      if (showChoice("Sei sicuro di voler chiudere l'applicazione?", "Chiudere l'applicazione?") == JOptionPane.YES_OPTION) {
        askToController(EXIT_APP, None)
      }
    }
  }
}
