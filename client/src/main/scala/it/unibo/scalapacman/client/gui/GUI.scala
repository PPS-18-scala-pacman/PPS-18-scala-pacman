package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, CardLayout, Dimension}

import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.gui.View.{MENU, OPTIONS, PLAY, STATS}
import javax.swing.{JFrame, JPanel, WindowConstants}

object GUI {

  implicit val controller: Controller = Controller()

  private val frame: JFrame = new JFrame

  private val HEIGHT: Int = 960
  private val WIDTH: Int = 720

  private val mainLayout: CardLayout = new CardLayout
  private val mainPanel = new JPanel(mainLayout)

  private val menuView: MenuView = MenuView()
  private val playView: PlayView = PlayView()
  private val optionsView: OptionsView = OptionsView(playView)
  private val statsView: StatsView = StatsView()

  mainPanel add(menuView, MENU.name)
  mainPanel add(playView, PLAY.name)
  mainPanel add(optionsView, OPTIONS.name)
  mainPanel add(statsView, STATS.name)

  changeView(MENU.name)

  frame add(mainPanel, BorderLayout.CENTER)

  frame setTitle "Scala Pacman"
  frame setSize new Dimension(WIDTH, HEIGHT)
  frame setResizable false
  frame setDefaultCloseOperation WindowConstants.EXIT_ON_CLOSE
  frame setLocationRelativeTo null // scalastyle:ignore null
  frame setVisible true

  def changeView(viewName: String): Unit = mainLayout show (mainPanel, viewName)
}
