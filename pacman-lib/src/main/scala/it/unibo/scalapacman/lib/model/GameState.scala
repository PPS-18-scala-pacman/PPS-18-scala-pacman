package it.unibo.scalapacman.lib.model

case class GameState(score: Int, ghostInFear:Boolean = false, pacmanEmpowered:Boolean = false) {
  def +(gameState: GameState): GameState = {
    GameState(
      score = score + gameState.score
    )
  }
}
