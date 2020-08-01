package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.model.{Character, Eatable, GameObject, GameState, Ghost, Map, Pacman, Tile}
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

  def calculateDeaths(characters: List[Character], gameState: GameState)(implicit collisions: List[(Character, GameObject)]): List[Character] =
    for (
      character <- characters
    ) yield calculateDeath(character, gameState, collisions.collect({ case (c1: Character, c2: Character) => (c1, c2) }))

  private def calculateDeath(character: Character, gameState: GameState, collisions: List[(Character, Character)]): Character =
    if (gameState.ghostInFear) {
      character match {
        case g@Ghost(_, _, _, _, _) => g.copy(isDead = collisions.exists { case (Pacman(_, _, _, _), ghost) if g.eq(ghost) => true })
        case _ => character
      }
    } else {
      character match {
        case p@Pacman(_, _, _, _) => p.copy(isDead = collisions.exists { case (pacman, Ghost(_, _, _, _, _)) if p.eq(pacman) => true })
        case _ => character
      }
    }

  // Se pacman è empowerato cambiano le velocità sia dei personaggi che dei fantasmi
  // Se i fantasmi sono nel tunnel rallentano la velocità
  // Se pacman mangia un pellet salta 1 tick e se mangia un fantasma salta 3 tick
  // Per adesso quest'ultima cosa non la implemento
  def calculateSpeeds(characters: List[Character], gameState: GameState, map: Map)(implicit collisions: List[(Character, GameObject)]): List[Character] = ???
}
