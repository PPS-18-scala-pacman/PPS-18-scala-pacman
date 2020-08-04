package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.model.LevelState.LevelState

case class GameState(score: Int,
                     ghostInFear: Boolean = false,
                     pacmanEmpowered: Boolean = false,
                     levelState: LevelState = LevelState.ONGOING)
