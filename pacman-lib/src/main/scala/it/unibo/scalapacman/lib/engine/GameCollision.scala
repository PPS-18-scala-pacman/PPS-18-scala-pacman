package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.model.{Character, GameObject, Map, Pacman}
import it.unibo.scalapacman.lib.engine.GameHelpers.CharacterHelper

object GameCollision {
  def collisions(characters: List[Character])(implicit map: Map): List[GameObject] =
    characters.filter(isPacman).flatMap(characterCollisions(characters))

  private def characterCollisions(characters: List[Character])(character: Character)(implicit map: Map): List[GameObject] =
    character.tile.eatable ++: characters.filter(_ != character).filter(_.tile eq character.tile)

  private def isPacman(character: Character): Boolean = character match {
    case Pacman(_, _, _) => true
    case _ => false
  }
}
