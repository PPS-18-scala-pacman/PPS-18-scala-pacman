package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.model.{Character, Dot, Eatable, Fruit, GameObject, GameState, GameTimedEvent,
  Level, LevelState, Map, SpeedCondition, Tile}
import it.unibo.scalapacman.lib.model.GameTimedEventType.{FRUIT_STOP, GHOST_RESTART, FRUIT_SPAWN, ENERGIZER_STOP, GameTimedEventType}
import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper, MapHelper}
import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.lib.model.Level.{ghostSpeed, pacmanSpeed}
import it.unibo.scalapacman.lib.model.LevelState.LevelState
import it.unibo.scalapacman.lib.model.SpeedCondition.SpeedCondition

import scala.concurrent.duration.FiniteDuration

/**
 * Steps to be called at every game tick in the following order:
 * - calculateBackToLife
 *
 */
object GameTick {
  def collisions(characters: List[Character])(implicit map: Map): List[(Character, GameObject)] = {
    def characterCollisions(character: Character): List[(Character, GameObject)] =
      (character.tile.eatable ++: characters.filter(_ != character).filter(_.tileIndexes == character.tileIndexes)).map(obj => (character, obj))

    characters collect { case p: Pacman => p } flatMap characterCollisions
  }

  def calculateGameState(gameState: GameState)(implicit collisions: List[(Character, GameObject)]): GameState = gameState.copy(
    score = (collisions collect { case (_, e: Eatable) => e } map (_.points) sum) + gameState.score,
    ghostInFear = gameState.ghostInFear || isEnergizerEaten,
    pacmanEmpowered = gameState.pacmanEmpowered || isEnergizerEaten,
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

  def calculateDeaths(characters: List[Character], gameState: GameState)(implicit collisions: List[(Character, GameObject)], map: Map): List[Character] =
    for (
      character <- characters
    ) yield calculateDeath(character, gameState, collisions.collect({ case (c1: Character, c2: Character) => (c1, c2) }))

  private def calculateDeath(character: Character, gameState: GameState, collisions: List[(Character, Character)])(implicit map: Map): Character = {
    def pacmanCollideGhost(pacman: Pacman)(collision: (Character, Character)): Boolean = collision match {
      case (pacman: Pacman, _: Ghost) if pacman.eq(pacman) => true
      case _ => false
    }
    def killGhost(ghost: Ghost): Ghost = ghost.copy(isDead = true, position = Map.getRestartPosition(map.mapType, Ghost, Some(ghost.ghostType)))
    def killPacman(pacman: Pacman): Pacman = pacman.copy(isDead = true, position = Map.getRestartPosition(map.mapType, Pacman))

    if (ghostCanBeKilled(gameState)) {
      character match {
        case ghost: Ghost if collisions.exists(_._2 eq ghost) => killGhost(ghost)
        case _ => character
      }
    } else {
      character match {
        case pacman: Pacman if collisions.exists(pacmanCollideGhost(pacman)) => killPacman(pacman)
        case _ => character
      }
    }
  }

  private def ghostCanBeKilled(gameState: GameState) = gameState.ghostInFear

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
    levelState = calculateLevelState(characters.collect { case p@Pacman(_, _, _, _) => p }, map.dots)
  )

  private def calculateLevelState(pacmans: List[Pacman], dots: Seq[((Int, Int), Dot.Dot)]): LevelState = (pacmans, dots) match {
    case (_, dots) if dots.isEmpty => LevelState.VICTORY
    case (pacmans, _) if pacmans forall (_.isDead == true) => LevelState.DEFEAT
    case _ => LevelState.ONGOING
  }

  def consumeTimeOfGameEvents(gameEvents: List[GameTimedEvent[Any]], timeMs: FiniteDuration): List[GameTimedEvent[Any]] =
    consumeTimeOfGameEvents(gameEvents, timeMs.toMillis)

  def consumeTimeOfGameEvents(gameEvents: List[GameTimedEvent[Any]], timeMs: Double): List[GameTimedEvent[Any]] =
    gameEvents.map(event => event.copy(
      timeMs = event.timeMs.map(_ - timeMs),
    ))

  def removeTimedOutGameEvents(gameEvents: List[GameTimedEvent[Any]])(implicit map: Map): List[GameTimedEvent[Any]] =
    gameEvents.filter(!filterTimedOutEvent(map)(_))

  def updateEvents(
                    gameEvents: List[GameTimedEvent[Any]],
                    gameState: GameState,
                    collisions: List[(Character, GameObject)]
                  )
                  (implicit map: Map): List[GameTimedEvent[_]] = {
    val FRUIT_TIMER = 9000
    def chainedEvent(gameEvent: GameTimedEvent[_]): Option[GameTimedEvent[_]] = gameEvent match {
      case GameTimedEvent(FRUIT_SPAWN, _, _, _) => Some(GameTimedEvent(FRUIT_STOP, timeMs = Some(FRUIT_TIMER)))
      case _ => None
    }

    def energizerStop: Option[GameTimedEvent[_]] =
      if (isEnergizerEaten(collisions)) {
        Some(GameTimedEvent(ENERGIZER_STOP, timeMs = Some(Level.energizerDuration(gameState.levelNumber))))
      } else {
        None
      }

    def ghostResume: List[Option[GameTimedEvent[_]]] =
      if (ghostCanBeKilled(gameState)) {
        collisions.map(_._2).collect { case ghost: Ghost => ghost } .map(ghost => Some(GameTimedEvent(
          GHOST_RESTART,
          dots = Some(map.dots.size - Level.ghostRespawnDotCounter(gameState.levelNumber, ghost.ghostType)),
          payload = Some(ghost.ghostType))
        ))
      } else {
        Nil
      }

    gameEvents :::
      (
        gameEvents.filter(filterTimedOutEvent(map)).map(chainedEvent) :::
          ghostResume :::
          energizerStop :: Nil
        ).filter(_ isDefined).map(_ get)
  }

  /**
   * Fa partire o ripartire i fantasmi
   *
   * @param gameEvents
   * @param characters
   * @return
   */
  def handleEvents(gameEvents: List[GameTimedEvent[Any]], characters: List[Character])(implicit map: Map): List[Character] = {
    def handleEvent(gameEvent: GameTimedEvent[Any], characters: List[Character])(implicit map: Map): List[Character] = gameEvent match {
      case GameTimedEvent(GHOST_RESTART, _, _, Some(ghostType: GhostType)) => characters.map {
        case ghost: Ghost if ghost.ghostType == ghostType => ghost.copy(isDead = false)
        case character: Character => character
      }
      case _ => characters
    }

    gameEvents.filter(filterTimedOutEvent(map))
      .foldRight(characters)(handleEvent)
  }

  /**
   * Fa spawnare o distrugge i frutti
   *
   * @param gameEvents
   * @param map
   * @return
   */
  def handleEvents(gameEvents: List[GameTimedEvent[Any]], map: Map): Map = {
    def handleEvent(gameEvent: GameTimedEvent[Any], map: Map): Map = gameEvent match {
      case GameTimedEvent(FRUIT_SPAWN, _, _, fruit@Some(Fruit.Fruit(_))) => map.putEatable(
        Map getFruitMapIndexes map.mapType,
        fruit.asInstanceOf[Some[Fruit.Fruit]]
      )
      case GameTimedEvent(FRUIT_STOP, _, _, _) => map empty Map.getFruitMapIndexes(map.mapType)
      case _ => map
    }

    gameEvents.filter(filterTimedOutEvent(map))
      .foldRight(map)(handleEvent)
  }

  /**
   * Aggiorna ghostInFear e pacmanEmpowered
   *
   * @param gameEvents
   * @param gameState
   * @return
   */
  def handleEvents(gameEvents: List[GameTimedEvent[Any]], gameState: GameState)(implicit map: Map): GameState = {
    def handleEvent(gameEvent: GameTimedEvent[Any], gameState: GameState): GameState = gameEvent match {
      case GameTimedEvent(ENERGIZER_STOP, _, _, _) => gameState.copy(ghostInFear = false, pacmanEmpowered = false)
      case _ => gameState
    }

    gameEvents.filter(filterTimedOutEvent(map))
      .foldRight(gameState)(handleEvent)
  }

  private def filterTimedOutEvent(map: Map)(gameEvent: GameTimedEvent[Any]): Boolean =
    !gameEvent.timeMs.exists(_ > 0) && !gameEvent.dots.exists(_ <= map.dots.size)

}
