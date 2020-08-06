package it.unibo.scalapacman.lib.ai

import it.unibo.scalapacman.lib.model.{Direction, Ghost, GhostType, Level, Map, Pacman}
import it.unibo.scalapacman.lib.ai.GhostAI.prologEngine
import it.unibo.scalapacman.lib.math.{Point2D, TileGeography}
import it.unibo.scalapacman.lib.model.Map.{emptyTrack, wall}
import org.scalatest.wordspec.AnyWordSpec

class GhostAITest extends AnyWordSpec {
  val MAP_SIZE = 5
  implicit val MAP: Map = Map(
    List.tabulate(MAP_SIZE)(wall) ::
      (wall() :: List.tabulate(3)(emptyTrack) ::: wall() :: Nil) ::
      (emptyTrack() :: emptyTrack() :: wall() :: emptyTrack() :: emptyTrack() :: Nil) ::
      (wall() :: List.tabulate(3)(emptyTrack) ::: wall() :: Nil) ::
      List.tabulate(MAP_SIZE)(wall) ::
      Nil
  )
  val ORIGIN: Point2D = Point2D(TileGeography.SIZE, TileGeography.SIZE)

  "Ghost AI" should {
    "calculate the shortest path" when {
      "target tile is next to starting one" in {
        assert(GhostAI.shortestPath(Ghost.clyde(ORIGIN, 1.0, Direction.WEST), (2, 1)) == (1, 1) :: (2, 1) :: Nil)
      }
      "target tile is after the tunnel" in {
        val path = GhostAI.shortestPath(Ghost.pinky(ORIGIN + Point2D(0, TileGeography.SIZE), 1.0, Direction.WEST), (3, 2))
        assert(path == (1, 2) :: (0, 2) :: (4, 2) :: (3, 2) :: Nil)
      }
      "target tile is behind a wall" in {
        val path = GhostAI.shortestPath(Ghost.clyde(ORIGIN + Point2D(TileGeography.SIZE, 0), 1.0, Direction.WEST), (2, 3))
        assert(path == (2, 1) :: (3, 1) :: (3, 2) :: (3, 3) :: (2, 1) :: Nil || path == (2, 1) :: (1, 1) :: (1, 2) :: (1, 3) :: (2, 3) :: Nil)
      }
    }
    "calculate the desired direction" when {
      "target is in front of the ghost" in {
        var blinky = Ghost.blinky(ORIGIN, 1.0, Direction.NORTH)
        var pacman = Pacman(ORIGIN + Point2D(0, TileGeography.SIZE), 1.0, Direction.SOUTH)
        assert(GhostAI.desiredDirection(blinky, pacman) == Direction.SOUTH)

        blinky = Ghost.blinky(ORIGIN + Point2D(0, TileGeography.SIZE), 1.0, Direction.NORTH)
        pacman = Pacman(ORIGIN, 1.0, Direction.SOUTH)
        assert(GhostAI.desiredDirection(blinky, pacman) == Direction.NORTH)
      }
      "target is after the tunnel" in {
        val blinky = Ghost.blinky(ORIGIN + Point2D(0, TileGeography.SIZE), 1.0, Direction.EAST)
        val pacman = Pacman(ORIGIN + Point2D(TileGeography.SIZE * 2, TileGeography.SIZE), 1.0, Direction.WEST)
        assert(GhostAI.desiredDirection(blinky, pacman) == Direction.WEST)
      }
      "target is behind a wall" in {
        val blinky = Ghost.blinky(ORIGIN + Point2D(TileGeography.SIZE, 0), 1.0, Direction.EAST)
        val pacman = Pacman(ORIGIN + Point2D(TileGeography.SIZE, TileGeography.SIZE * 2), 1.0, Direction.EAST)
        val desiredDirection = GhostAI.desiredDirection(blinky, pacman)
        assert(desiredDirection == Direction.EAST || desiredDirection == Direction.WEST)
      }
      "blinky is at the first curve and choose the correct direction" in {
        val generator = Level.Classic(1)
        val map = generator.map
        var blinky = generator.ghost(GhostType.BLINKY)
        blinky = blinky.copy(position = blinky.position + Point2D(TileGeography.SIZE * -4, 0)) // scalastyle:ignore magic.number
        val pacman = generator.pacman
        val desiredDirection = GhostAI.desiredDirection(blinky, pacman)(prologEngine, map)
        assert(desiredDirection == Direction.SOUTH)
      }
    }
  }
}
