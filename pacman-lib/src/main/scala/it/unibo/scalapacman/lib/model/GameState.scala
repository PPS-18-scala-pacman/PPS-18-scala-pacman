package it.unibo.scalapacman.lib.model

case class GameState(points: Long) {
  def +(gameState: GameState): GameState = {
    GameState(
      points = points + gameState.points
    )
  }
}
