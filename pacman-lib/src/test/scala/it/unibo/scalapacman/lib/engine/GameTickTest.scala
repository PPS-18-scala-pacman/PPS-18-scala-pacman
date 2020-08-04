package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.math.{Point2D, TileGeography}
import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit, GameState, Ghost, LevelState, Map, Pacman, Tile}
import it.unibo.scalapacman.lib.engine.GameTick.{calculateDeaths, calculateGameState, calculateLevelState, calculateMap, calculateSpeeds, collisions}
import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper, MapHelper}
import org.scalatest.wordspec.AnyWordSpec

class GameTickTest extends AnyWordSpec {
  val GHOST_1: Ghost = Ghost.blinky(Point2D(0, 0), 0.0, Direction.WEST)
  val GHOST_2: Ghost = Ghost.inky(Point2D(1, 1), 0.0, Direction.EAST)
  val GHOST_3: Ghost = Ghost.pinky(Point2D(TileGeography.SIZE, 0), 0.0, Direction.EAST)
  val PACMAN: Pacman = Pacman(Point2D(0, 0), 0.0, Direction.NORTH)
  val MAP_SIZE = 4
  implicit val MAP: Map = Map(
    List(
      List.tabulate(MAP_SIZE)(Map.emptyTrack),
      List.tabulate(MAP_SIZE)(Map.smallDot),
      List.tabulate(MAP_SIZE)(Map.energizerDot),
      List.tabulate(MAP_SIZE)(_ => Tile.Track(Some(Fruit.APPLE)))
    )
  )
  val OLD_GAME_STATE: GameState = GameState(1)

  "Every game tick, the game" should {
    "evaluate collisions" which {
      "return no results" when {
        "there are only ghosts" in {
          assert(collisions(List(GHOST_1, GHOST_2)).isEmpty)
        }
        "Pacman is alone in its tile" in {
          val pacman = Pacman(PACMAN.position + Point2D(TileGeography.SIZE, 0), 0.0, Direction.EAST)
          assert(collisions(List(pacman, GHOST_1, GHOST_2)).isEmpty)
        }
        "Pacman's tile is empty" in {
          assert(collisions(List(PACMAN)).isEmpty)
        }
      }
      "return ghosts" when {
        "they are in the same position" in {
          val coll = collisions(List(PACMAN, GHOST_1))
          assert(coll.size == 1 && coll.head._2 == GHOST_1)
        }
        "they are in a different position but in the same tile" in {
          val coll = collisions(List(PACMAN, GHOST_2))
          assert(coll.size == 1 && coll.head._2 == GHOST_2)
        }
        "they are more than one in Pacman's tile" in {
          assert(collisions(List(PACMAN, GHOST_1, GHOST_2)).size == 2)
        }
      }
      "return the tile's game object" when {
        "a fruit is in the Pacman's tile" in {
          val pacman = Pacman(PACMAN.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.SOUTH)
          val coll = collisions(List(pacman))
          assert(coll.size == 1 && coll.head._2 == Dot.SMALL_DOT)
        }
        "a small dot is in the Pacman's tile" in {
          val pacman = Pacman(PACMAN.position + Point2D(0, TileGeography.SIZE * 2), 0.0, Direction.NORTH)
          val coll = collisions(List(pacman))
          assert(coll.size == 1 && coll.head._2 == Dot.ENERGIZER_DOT)
        }
        "an energized dot is in the Pacman's tile" in {
          val pacman = Pacman(PACMAN.position + Point2D(0, TileGeography.SIZE * 3), 0.0, Direction.WEST)
          val coll = collisions(List(pacman))
          assert(coll.size == 1 && coll.head._2 == Fruit.APPLE)
        }
      }
      "return ghosts and tile's game object" when {
        "they are all in the Pacman's tile" in {
          var pacman = Pacman(PACMAN.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.EAST)
          var ghost1 = Ghost(GHOST_1.ghostType, GHOST_1.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.EAST)
          var ghost2 = Ghost(GHOST_2.ghostType, GHOST_2.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.EAST)
          assert(collisions(List(pacman, ghost1, ghost2)).size == 3)

          pacman = Pacman(pacman.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.EAST)
          ghost1 = Ghost(ghost1.ghostType, ghost1.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.EAST)
          ghost2 = Ghost(ghost2.ghostType, ghost2.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.EAST)
          assert(collisions(List(pacman, ghost1, ghost2)).size == 3)
        }
      }
    }
    "evaluate the new game state" which {
      "calculate gained points" when {
        "pacman collide with a small dot" in {
          assertResult(OLD_GAME_STATE.score + Dot.SMALL_DOT.points)(calculateGameState(OLD_GAME_STATE)((PACMAN, Dot.SMALL_DOT) :: Nil).score)
        }
        "pacman collide with an energizer dot" in {
          assertResult(OLD_GAME_STATE.score + Dot.ENERGIZER_DOT.points)(calculateGameState(OLD_GAME_STATE)((PACMAN, Dot.ENERGIZER_DOT) :: Nil).score)
        }
        "pacman collide with a fruit" in {
          assertResult(OLD_GAME_STATE.score + Fruit.APPLE.points)(calculateGameState(OLD_GAME_STATE)((PACMAN, Fruit.APPLE) :: Nil).score)
          assertResult(OLD_GAME_STATE.score + Fruit.BELL.points)(calculateGameState(OLD_GAME_STATE)((PACMAN, Fruit.BELL) :: Nil).score)
          assertResult(OLD_GAME_STATE.score + Fruit.CHERRIES.points)(calculateGameState(OLD_GAME_STATE)((PACMAN, Fruit.CHERRIES) :: Nil).score)
          assertResult(OLD_GAME_STATE.score + Fruit.GALAXIAN.points)(calculateGameState(OLD_GAME_STATE)((PACMAN, Fruit.GALAXIAN) :: Nil).score)
          assertResult(OLD_GAME_STATE.score + Fruit.GRAPES.points)(calculateGameState(OLD_GAME_STATE)((PACMAN, Fruit.GRAPES) :: Nil).score)
          assertResult(OLD_GAME_STATE.score + Fruit.KEY.points)(calculateGameState(OLD_GAME_STATE)((PACMAN, Fruit.KEY) :: Nil).score)
          assertResult(OLD_GAME_STATE.score + Fruit.PEACH.points)(calculateGameState(OLD_GAME_STATE)((PACMAN, Fruit.PEACH) :: Nil).score)
          assertResult(OLD_GAME_STATE.score + Fruit.STRAWBERRY.points)(calculateGameState(OLD_GAME_STATE)((PACMAN, Fruit.STRAWBERRY) :: Nil).score)
        }
        "in any other case" in {
          assertResult(OLD_GAME_STATE.score)(calculateGameState(OLD_GAME_STATE)(Nil).score)
        }
      }
      "calculate when ghost are feared and pacman is empowered" when {
        "pacman eat an energizer dot" in {
          val gameState = calculateGameState(OLD_GAME_STATE)((PACMAN, Dot.ENERGIZER_DOT) :: Nil)
          assert(gameState.ghostInFear == !OLD_GAME_STATE.ghostInFear && gameState.pacmanEmpowered == !OLD_GAME_STATE.pacmanEmpowered)
        }
        "in any other case" in {
          val gameState = calculateGameState(OLD_GAME_STATE)(Nil)
          assert(gameState.ghostInFear == OLD_GAME_STATE.ghostInFear && gameState.pacmanEmpowered == OLD_GAME_STATE.pacmanEmpowered)
        }
      }
      "calculate the level state" when {
        "pacman is dead and is defeated" in {
          assert(OLD_GAME_STATE.levelState != LevelState.DEFEAT)
          val characters = PACMAN.copy(isDead = true) :: GHOST_1 :: GHOST_3 :: Nil
          val gameState = calculateLevelState(OLD_GAME_STATE, characters, MAP)
          assert(gameState.levelState == LevelState.DEFEAT)
        }
        "all dots are eaten and the level is won" in {
          assert(OLD_GAME_STATE.levelState != LevelState.VICTORY)
          val characters = PACMAN :: GHOST_1 :: GHOST_3 :: Nil
          val map = Map(tiles = List[List[Tile]](Tile.TrackTunnel() :: Tile.Track(None) :: Nil))
          val gameState = calculateLevelState(OLD_GAME_STATE, characters, map)
          assert(gameState.levelState == LevelState.VICTORY)
        }
        "is ongoing" in {
          assert(OLD_GAME_STATE.levelState == LevelState.ONGOING)
          val characters = PACMAN :: GHOST_1 :: GHOST_3 :: Nil
          val gameState = calculateLevelState(OLD_GAME_STATE, characters, MAP)
          assert(gameState.levelState == LevelState.ONGOING)
        }
      }
    }
    "evaluate the new map" which {
      "remove eaten fruits" in {
        val pacman = PACMAN.copy(position = Point2D(TileGeography.SIZE * 3, TileGeography.SIZE * 3))
        val newMap = calculateMap(MAP)((pacman, Fruit.APPLE) :: Nil)
        for (y <- 0 until MAP.height; x <- 0 until MAP.width) {
          val oldTile = MAP.tiles(y)(x)
          val newTile = newMap.tiles(y)(x)
          if (pacman.tileIndexes == (x, y)) assert(oldTile.eatable.isDefined && newTile.eatable.isEmpty) else assert(oldTile == newTile)
        }
      }
      "remove eaten dots" in {
        val pacman = PACMAN.copy(position = Point2D(TileGeography.SIZE, TileGeography.SIZE))
        val newMap = calculateMap(MAP)((pacman, Dot.SMALL_DOT) :: Nil)
        for (y <- 0 until MAP.height; x <- 0 until MAP.width) {
          val oldTile = MAP.tiles(y)(x)
          val newTile = newMap.tiles(y)(x)
          if (pacman.tileIndexes == (x, y)) assert(oldTile.eatable.isDefined && newTile.eatable.isEmpty) else assert(oldTile == newTile)
        }
        val pacman2 = PACMAN.copy(position = Point2D(TileGeography.SIZE * 2, TileGeography.SIZE * 2))
        val newMap2 = calculateMap(MAP)((pacman2, Dot.ENERGIZER_DOT) :: Nil)
        for (y <- 0 until MAP.height; x <- 0 until MAP.width) {
          val oldTile = MAP.tiles(y)(x)
          val newTile = newMap2.tiles(y)(x)
          if (pacman2.tileIndexes == (x, y)) assert(oldTile.eatable.isDefined && newTile.eatable.isEmpty) else assert(oldTile == newTile)
        }
      }
      "is the same when there are no collisions or only collisions with other characters" in {
        val noCollisions = Nil
        assertResult(MAP)(calculateMap(MAP)(noCollisions))
        val charactersCollisions = (PACMAN, GHOST_1) :: Nil
        assertResult(MAP)(calculateMap(MAP)(charactersCollisions))
      }
    }
    "evaluate the new characters" which {
      "kill pacman" when {
        "it collide with a ghost not in fear" in {
          val characters = PACMAN :: GHOST_1 :: Nil
          val state = GameState(0)
          val noCollisions = Nil
          assert(calculateDeaths(characters, state)(noCollisions) == characters)
          val charactersCollisions = (PACMAN, GHOST_1) :: Nil
          assert(calculateDeaths(characters, state)(charactersCollisions) == PACMAN.copy(isDead = true) :: GHOST_1 :: Nil)
        }
      }
      "kill a ghost" when {
        "it is in fear and collide with pacman" in {
          val characters = PACMAN :: GHOST_1 :: Nil
          val state = GameState(0, ghostInFear = true)
          val noCollisions = Nil
          assert(calculateDeaths(characters, state)(noCollisions) == characters)
          val charactersCollisions = (PACMAN, GHOST_1) :: Nil
          assert(calculateDeaths(characters, state)(charactersCollisions) == PACMAN :: GHOST_1.copy(isDead = true) :: Nil)
        }
      }
      "change the speed" when {
        "pacman is empowered" in {
          var gameState = GameState(0, pacmanEmpowered = false, ghostInFear = false)
          val noCollisions = Nil
          val characters = calculateSpeeds(PACMAN :: GHOST_1 :: GHOST_3 :: Nil, 1, gameState)(noCollisions, MAP)
          gameState = GameState(0, pacmanEmpowered = true, ghostInFear = true)
          val newCharacters = calculateSpeeds(characters, 1, gameState)(noCollisions, MAP)
          assert(newCharacters.head.speed > characters.head.speed)
          assert(newCharacters(1).speed < characters(1).speed)
          assert(newCharacters(2).speed < characters(2).speed)
        }
        "a ghost is in the tunnel" in {
          var gameState = GameState(0, pacmanEmpowered = false, ghostInFear = false)
          val noCollisions = Nil
          val characters = calculateSpeeds(PACMAN :: GHOST_1 :: GHOST_3 :: Nil, 1, gameState)(noCollisions, MAP)
          val map = Map(tiles = List[List[Tile]](Tile.TrackTunnel() :: Tile.Track(None) :: Nil))
          gameState = GameState(0, pacmanEmpowered = true, ghostInFear = true)
          val newCharacters = calculateSpeeds(characters, 1, GameState(0))(noCollisions, map)
          assert(newCharacters.head == characters.head)
          assert(newCharacters(1).speed < characters(1).speed)
          assert(newCharacters(2) == characters(2))
        }
      }
    }
  }
}
