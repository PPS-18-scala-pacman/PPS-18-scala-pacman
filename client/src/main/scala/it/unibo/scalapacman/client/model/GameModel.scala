package it.unibo.scalapacman.client.model

import it.unibo.scalapacman.client.input.KeyMap
import it.unibo.scalapacman.lib.model.Map

case class GameModel(
                      gameId: Option[String],
                      keyMap: KeyMap,
                      map: Map,
                    )
