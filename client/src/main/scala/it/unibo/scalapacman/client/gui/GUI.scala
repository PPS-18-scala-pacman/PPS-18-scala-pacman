package it.unibo.scalapacman.client.gui

import java.awt.{BorderLayout, CardLayout, Dimension}

import javax.swing.{JFrame, JPanel}

class GUI extends JFrame {
  private final val HEIGHT = 960
  private final val WIDTH = 720
  private val mainLayout = new CardLayout
  private val mainPanel = new JPanel(mainLayout)

  private val menu = new MainMenu

  mainPanel add(menu, "menu")
  mainLayout show (mainPanel, "menu")

  add(mainPanel, BorderLayout.CENTER)

  setTitle("Scala Pacman")
  setSize(new Dimension(WIDTH, HEIGHT))
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  //noinspection ScalaStyle
  setLocationRelativeTo(null)
  setVisible(true)
}
