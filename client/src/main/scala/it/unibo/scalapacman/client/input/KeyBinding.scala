package it.unibo.scalapacman.client.input

trait KeyBinding[A] {
  var UP: A
  var DOWN: A
  var RIGHT: A
  var LEFT: A
}
