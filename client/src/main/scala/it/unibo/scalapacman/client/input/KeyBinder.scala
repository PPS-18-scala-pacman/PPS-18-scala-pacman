package it.unibo.scalapacman.client.input

trait KeyBinder {
  def applyKeyBinding(keyMap: KeyMap): Unit
}
