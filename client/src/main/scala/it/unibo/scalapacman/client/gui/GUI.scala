package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, CardLayout, Dimension}

import javax.swing.{JFrame, JPanel, WindowConstants}

object GUI {

  object ViewsName {
    val MENU_VIEW: String = "menu"
    val PLAY_VIEW: String = "play"
    val OPTIONS_VIEW: String = "options"
    val STATS_VIEW: String = "stats"
  }

  private val frame: JFrame = new JFrame

  private val HEIGHT: Int = 960
  private val WIDTH: Int = 720

  private val mainLayout: CardLayout = new CardLayout
  private val mainPanel = new JPanel(mainLayout)

  private val menuView = new MenuView
  private val playView = new PlayView
  private val optionsView = new OptionsView
  private val statsView = new StatsView

  mainPanel add(menuView, ViewsName.MENU_VIEW)
  mainPanel add(playView, ViewsName.PLAY_VIEW)
  mainPanel add(optionsView, ViewsName.OPTIONS_VIEW)
  mainPanel add(statsView, ViewsName.STATS_VIEW)

  changeView(ViewsName.MENU_VIEW)

  frame add(mainPanel, BorderLayout.CENTER)

  frame setTitle "Scala Pacman"
  frame setSize new Dimension(WIDTH, HEIGHT)
  frame setResizable false
  frame setDefaultCloseOperation WindowConstants.EXIT_ON_CLOSE
  frame setLocationRelativeTo null // scalastyle:ignore null
  frame setVisible true

  def changeView(viewName: String): Unit = mainLayout show (mainPanel, viewName)
}
