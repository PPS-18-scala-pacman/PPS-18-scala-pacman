package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.model.{Character, Eatable, GameObject, GameState, Map, Pacman, Tile}
import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper, MapHelper}

object GameTick {
  def collisions(characters: List[Character])(implicit map: Map): List[(Character, GameObject)] =
    characters collect { case p: Pacman => p } flatMap characterCollisions(characters)

  private def characterCollisions(characters: List[Character])(character: Character)(implicit map: Map): List[(Character, GameObject)] =
    (character.tile.eatable ++: characters.filter(_ != character).filter(_.tile eq character.tile)).map(obj => (character, obj))

  def calculateGameState(gameState: GameState)(implicit collisions: List[(Character, GameObject)]): GameState = GameState(
    score = (collisions collect { case (_, e: Eatable) => e } map (_.points) sum) + gameState.score
  )

  def calculateMap(map: Map)(implicit collisions: List[(Character, GameObject)]): Map = map.copy(
    tiles = calculateMapTiles(map, collisions.collect({ case (a: Character, e: Eatable) => (a, e) }))
  )

  private def calculateMapTiles(map: Map, collisions: List[(Character, Eatable)]): List[List[Tile]] =
    collisions.map(_._1).foldLeft(map)(calculateMapTile(_, _)).tiles

  private def calculateMapTile(implicit map: Map, character: Character): Map = character.eat
}
