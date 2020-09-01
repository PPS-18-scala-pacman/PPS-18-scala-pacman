package it.unibo.scalapacman.client.input

import java.awt.event.KeyEvent

sealed trait JavaKeyBinding extends KeyBinding[Int]

object JavaKeyBinding {
  case object DefaultJavaKeyBinding extends JavaKeyBinding {
    var UP: Int = KeyEvent.VK_UP
    var DOWN: Int = KeyEvent.VK_DOWN
    var RIGHT: Int = KeyEvent.VK_RIGHT
    var LEFT: Int = KeyEvent.VK_LEFT
    var PAUSE: Int = KeyEvent.VK_P
  }
}
