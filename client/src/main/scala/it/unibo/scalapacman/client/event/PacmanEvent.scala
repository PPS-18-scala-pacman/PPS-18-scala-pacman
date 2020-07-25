package it.unibo.scalapacman.client.event

trait PacmanEvent

case class GameUpdate(map: List[List[Char]]) extends PacmanEvent
