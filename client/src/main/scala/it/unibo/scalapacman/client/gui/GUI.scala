package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, CardLayout, Dimension}
import javax.swing.{JFrame, JPanel, WindowConstants}

object GUI {
  private val frame: JFrame = new JFrame

  private val HEIGHT = 960
  private val WIDTH = 720
  private val mainLayout = new CardLayout
  private val mainPanel = new JPanel(mainLayout)

  private val menu = new MainMenu

  mainPanel add(menu, "menu")
  mainLayout show (mainPanel, "menu")

  frame add(mainPanel, BorderLayout.CENTER)

  frame setTitle "Scala Pacman"
  frame setSize new Dimension(WIDTH, HEIGHT)
  frame setDefaultCloseOperation WindowConstants.EXIT_ON_CLOSE
  //noinspection ScalaStyle
  frame setLocationRelativeTo null
  frame setVisible true

  def changePanel(page: String): Unit = mainLayout show (mainPanel, page)
}
