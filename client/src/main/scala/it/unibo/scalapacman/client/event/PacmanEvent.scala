package it.unibo.scalapacman.client.event

import it.unibo.scalapacman.client.input.KeyMap
import it.unibo.scalapacman.client.map.PacmanMap.PacmanMap
import it.unibo.scalapacman.lib.model.GameState

trait PacmanEvent

case class GameUpdate(map: PacmanMap, gameState: GameState) extends PacmanEvent
case class GamePaused(paused: Boolean) extends PacmanEvent
case class NewKeyMap(keyMap: KeyMap) extends PacmanEvent
