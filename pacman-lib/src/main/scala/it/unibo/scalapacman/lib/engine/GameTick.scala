package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.model.{Character, Dot, Eatable, GameObject, GameState, LevelState, Map, SpeedCondition, Tile}
import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper, MapHelper}
import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import it.unibo.scalapacman.lib.model.Level.{ghostSpeed, pacmanSpeed}
import it.unibo.scalapacman.lib.model.LevelState.LevelState
import it.unibo.scalapacman.lib.model.SpeedCondition.SpeedCondition

/**
 * Steps to be called at every game tick in the following order:
 * - calculateBackToLife
 *
 */
object GameTick {
  def collisions(characters: List[Character])(implicit map: Map): List[(Character, GameObject)] =
    characters collect { case p: Pacman => p } flatMap characterCollisions(characters)

  private def characterCollisions(characters: List[Character])(character: Character)(implicit map: Map): List[(Character, GameObject)] =
    (character.tile.eatable ++: characters.filter(_ != character).filter(_.tileIndexes == character.tileIndexes)).map(obj => (character, obj))

  def calculateGameState(gameState: GameState)(implicit collisions: List[(Character, GameObject)]): GameState = GameState(
    score = (collisions collect { case (_, e: Eatable) => e } map (_.points) sum) + gameState.score,
    ghostInFear = isEnergizerEaten,
    pacmanEmpowered = isEnergizerEaten,
  )

  private def isEnergizerEaten(implicit collisions: List[(Character, GameObject)]) =
    collisions.exists {
      case (_, Dot.ENERGIZER_DOT) => true
      case _ => false
    }

  def calculateMap(map: Map)(implicit collisions: List[(Character, GameObject)]): Map = map.copy(
    tiles = calculateMapTiles(map, collisions.collect({ case (a: Character, e: Eatable) => (a, e) }))
  )

  private def calculateMapTiles(map: Map, collisions: List[(Character, Eatable)]): List[List[Tile]] =
    collisions.map(_._1).foldLeft(map)(calculateMapTile(_, _)).tiles

  private def calculateMapTile(implicit map: Map, character: Character): Map = character.eat

//  def calculateBackToLife(characters: List[Character]): List[Character] =
//    for (
//      character <- characters
//    ) yield calculateBackToLife(character)
//
//  private def calculateBackToLife(character: Character): Character =
//    character match {
//      case g@Ghost(GhostType.BLINKY, _, _, _, true) => g.copy(position = g.deadPosition, isDead = false)
//      case g@Ghost(GhostType.PINKY, _, _, _, true) => g.copy(position = g.deadPosition, isDead = false)
//      case g@Ghost(GhostType.INKY, _, _, _, true) => g.copy(position = g.deadPosition, isDead = false)
//      case g@Ghost(GhostType.CLYDE, _, _, _, true) => g.copy(position = g.deadPosition, isDead = false)
//      case _ => character
//    }

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

  /**
   * Calcola la velocità dei personaggi.
   * Se pacman è empowered aumenta di velocità mentre diminuisce quella dei fantasmi.
   * Diminuisce sempre la velocità dei fantasmi che si trovano nel tunnel.
   *
   * @param characters Lista dei personaggi in gioco
   * @param gameState  Stato della partita
   * @param collisions Collisioni correnti
   * @param map        Mappa di gioco
   * @return
   */
  def calculateSpeeds(characters: List[Character], level: Int, gameState: GameState)
                     (implicit collisions: List[(Character, GameObject)], map: Map): List[Character] =
    characters.map(char => calculateSpeed(char, level, calculateSpeedCondition(char, gameState)))

  private def calculateSpeed(character: Character, level: Int, speedCondition: SpeedCondition)
                            (implicit collisions: List[(Character, GameObject)], map: Map): Character =
    character match {
      case pacman@Pacman(_, speed, _, _) => if (speed == pacmanSpeed(level, speedCondition)) pacman else pacman.copy(speed = pacmanSpeed(level, speedCondition))
      case ghost@Ghost(_, _, speed, _, _) => if (speed == ghostSpeed(level, speedCondition)) ghost else ghost.copy(speed = ghostSpeed(level, speedCondition))
      case _ => character
    }

  private def calculateSpeedCondition(character: Character, gameState: GameState)
                                     (implicit collisions: List[(Character, GameObject)], map: Map): SpeedCondition.Value =
    character match {
      case Pacman(_, _, _, _) if gameState.pacmanEmpowered => SpeedCondition.FRIGHT
      case Ghost(_, _, _, _, _) if character.tile.isInstanceOf[Tile.TrackTunnel] => SpeedCondition.TUNNEL
      case Ghost(_, _, _, _, _) if gameState.ghostInFear => SpeedCondition.FRIGHT
      case _ => SpeedCondition.NORM
    }

  def calculateLevelState(gameState: GameState, characters: List[Character], map: Map): GameState = gameState.copy(
    levelState = calculateLevelState(characters.collect { case p@Pacman(_, _, _, _) => p }, map.eatablesToSeq[Dot.Val])
  )

  private def calculateLevelState(pacmans: List[Pacman], dots: Seq[((Int, Int), Dot.Val)]): LevelState = (pacmans, dots) match {
    case (_, dots) if dots.isEmpty => LevelState.VICTORY
    case (pacmans, _) if pacmans forall (_.isDead == true) => LevelState.DEFEAT
    case _ => LevelState.ONGOING
  }
}
