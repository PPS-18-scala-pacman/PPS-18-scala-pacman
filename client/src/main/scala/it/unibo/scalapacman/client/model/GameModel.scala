package it.unibo.scalapacman.client.model

import it.unibo.scalapacman.client.input.KeyMap
import it.unibo.scalapacman.lib.model.Map

case class GameModel(
                      gameId: Option[String] = None,
                      paused: Boolean = false,
                      keyMap: KeyMap,
                      map: Map,
                    )
