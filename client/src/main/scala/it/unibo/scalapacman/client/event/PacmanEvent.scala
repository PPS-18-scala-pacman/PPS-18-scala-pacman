package it.unibo.scalapacman.client.event

import it.unibo.scalapacman.client.map.PacmanMap.PacmanMap

trait PacmanEvent

case class GameUpdate(map: PacmanMap) extends PacmanEvent
