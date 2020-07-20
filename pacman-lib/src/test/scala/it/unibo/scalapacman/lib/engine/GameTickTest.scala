package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.math.{Point2D, TileGeography}
import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit, GameState, Ghost, Map, Pacman, Tile}
import org.scalatest.wordspec.AnyWordSpec

class GameTickTest extends AnyWordSpec {
  val GHOST_1: Ghost = Ghost.blinky(Point2D(0, 0), 0.0, Direction.WEST)
  val GHOST_2: Ghost = Ghost.inky(Point2D(1, 1), 0.0, Direction.EAST)
  val PACMAN: Pacman = Pacman(Point2D(0, 0), 0.0, Direction.NORTH)
  val MAP_SIZE = 4
  implicit val MAP: Map = Map(
    List(
      List.tabulate(MAP_SIZE)(_ => Tile.Track(None)),
      List.tabulate(MAP_SIZE)(_ => Tile.Track(Some(Dot.SMALL_DOT))),
      List.tabulate(MAP_SIZE)(_ => Tile.Track(Some(Dot.ENERGIZER_DOT))),
      List.tabulate(MAP_SIZE)(_ => Tile.Track(Some(Fruit.APPLE)))
    )
  )
  val OLD_GAME_STATE: GameState = GameState(1)

  "Every game tick, the game" should {
    "evaluate collisions" which {
      "return no results" when {
        "there are only ghosts" in {
          assert(GameTick.collisions(List(GHOST_1, GHOST_2)).isEmpty)
        }
        "Pacman is alone in its tile" in {
          val pacman = Pacman(PACMAN.position + Point2D(TileGeography.SIZE, 0), 0.0, Direction.EAST)
          assert(GameTick.collisions(List(pacman, GHOST_1, GHOST_2)).isEmpty)
        }
        "Pacman's tile is empty" in {
          assert(GameTick.collisions(List(PACMAN)).isEmpty)
        }
      }
      "return ghosts" when {
        "they are in the same position" in {
          val collisions = GameTick.collisions(List(PACMAN, GHOST_1))
          assert(collisions.size == 1 && collisions.head == GHOST_1)
        }
        "they are in a different position but in the same tile" in {
          val collisions = GameTick.collisions(List(PACMAN, GHOST_2))
          assert(collisions.size == 1 && collisions.head == GHOST_2)
        }
        "they are more than one in Pacman's tile" in {
          assert(GameTick.collisions(List(PACMAN, GHOST_1, GHOST_2)).size == 2)
        }
      }
      "return the tile's game object" when {
        "a fruit is in the Pacman's tile" in {
          val pacman = Pacman(PACMAN.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.SOUTH)
          val collisions = GameTick.collisions(List(pacman))
          assert(collisions.size == 1 && collisions.head == Dot.SMALL_DOT)
        }
        "a small dot is in the Pacman's tile" in {
          val pacman = Pacman(PACMAN.position + Point2D(0, TileGeography.SIZE * 2), 0.0, Direction.NORTH)
          val collisions = GameTick.collisions(List(pacman))
          assert(collisions.size == 1 && collisions.head == Dot.ENERGIZER_DOT)
        }
        "an energized dot is in the Pacman's tile" in {
          val pacman = Pacman(PACMAN.position + Point2D(0, TileGeography.SIZE * 3), 0.0, Direction.WEST)
          val collisions = GameTick.collisions(List(pacman))
          assert(collisions.size == 1 && collisions.head == Fruit.APPLE)
        }
      }
      "return ghosts and tile's game object" when {
        "they are all in the Pacman's tile" in {
          var pacman = Pacman(PACMAN.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.EAST)
          var ghost1 = Ghost(GHOST_1.ghostType, GHOST_1.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.EAST)
          var ghost2 = Ghost(GHOST_2.ghostType, GHOST_2.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.EAST)
          assert(GameTick.collisions(List(pacman, ghost1, ghost2)).size == 3)

          pacman = Pacman(pacman.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.EAST)
          ghost1 = Ghost(ghost1.ghostType, ghost1.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.EAST)
          ghost2 = Ghost(ghost2.ghostType, ghost2.position + Point2D(0, TileGeography.SIZE), 0.0, Direction.EAST)
          assert(GameTick.collisions(List(pacman, ghost1, ghost2)).size == 3)
        }
      }
    }
    "evaluate the new game state" which {
      "calculate gained points" when {
        "pacman collide with a small dot" in {
          assertResult(OLD_GAME_STATE.points + Dot.SMALL_DOT.points)((OLD_GAME_STATE + GameTick.calculateGameState(Dot.SMALL_DOT :: Nil)).points)
        }
        "pacman collide with an energizer dot" in {
          assertResult(OLD_GAME_STATE.points + Dot.ENERGIZER_DOT.points)((OLD_GAME_STATE + GameTick.calculateGameState(Dot.ENERGIZER_DOT :: Nil)).points)
        }
        "pacman collide with a fruit" in {
          assertResult(OLD_GAME_STATE.points + Fruit.APPLE.points)((OLD_GAME_STATE + GameTick.calculateGameState(Fruit.APPLE :: Nil)).points)
          assertResult(OLD_GAME_STATE.points + Fruit.BELL.points)((OLD_GAME_STATE + GameTick.calculateGameState(Fruit.BELL :: Nil)).points)
          assertResult(OLD_GAME_STATE.points + Fruit.CHERRIES.points)((OLD_GAME_STATE + GameTick.calculateGameState(Fruit.CHERRIES :: Nil)).points)
          assertResult(OLD_GAME_STATE.points + Fruit.GALAXIAN.points)((OLD_GAME_STATE + GameTick.calculateGameState(Fruit.GALAXIAN :: Nil)).points)
          assertResult(OLD_GAME_STATE.points + Fruit.GRAPES.points)((OLD_GAME_STATE + GameTick.calculateGameState(Fruit.GRAPES :: Nil)).points)
          assertResult(OLD_GAME_STATE.points + Fruit.KEY.points)((OLD_GAME_STATE + GameTick.calculateGameState(Fruit.KEY :: Nil)).points)
          assertResult(OLD_GAME_STATE.points + Fruit.PEACH.points)((OLD_GAME_STATE + GameTick.calculateGameState(Fruit.PEACH :: Nil)).points)
          assertResult(OLD_GAME_STATE.points + Fruit.STRAWBERRY.points)((OLD_GAME_STATE + GameTick.calculateGameState(Fruit.STRAWBERRY :: Nil)).points)
        }
      }
    }
  }

}
