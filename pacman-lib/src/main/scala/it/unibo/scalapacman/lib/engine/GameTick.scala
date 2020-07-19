package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.model.{Character, Eatable, GameObject, GameState, Map, Pacman}
import it.unibo.scalapacman.lib.engine.GameHelpers.CharacterHelper

object GameTick {
  def collisions(characters: List[Character])(implicit map: Map): List[GameObject] =
    characters collect { case p: Pacman => p } flatMap characterCollisions(characters)

  private def characterCollisions(characters: List[Character])(character: Character)(implicit map: Map): List[GameObject] =
    character.tile.eatable ++: characters.filter(_ != character).filter(_.tile eq character.tile)

  def calculateGameState(collisions: List[GameObject]): GameState = GameState(
    points = collisions collect { case e: Eatable => e } map (_.points) sum
  )
}
