package it.unibo.scalapacman.lib.ai

import it.unibo.scalapacman.lib.model.{Direction, GhostType, Level, Map}
import it.unibo.scalapacman.lib.ai.GhostAI.prologEngine
import it.unibo.scalapacman.lib.math.{Point2D, TileGeography}
import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
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
        assert(GhostAI.shortestPath(Ghost(GhostType.CLYDE, ORIGIN, 1.0, Direction.WEST), (2, 1)) == (1, 1) :: (2, 1) :: Nil)
      }
      "target tile is after the tunnel" in {
        val path = GhostAI.shortestPath(Ghost(GhostType.PINKY, ORIGIN + Point2D(0, TileGeography.SIZE), 1.0, Direction.WEST), (3, 2))
        assert(path == (1, 2) :: (0, 2) :: (4, 2) :: (3, 2) :: Nil)
      }
      "target tile is behind a wall" in {
        val path = GhostAI.shortestPath(Ghost(GhostType.CLYDE, ORIGIN + Point2D(TileGeography.SIZE, 0), 1.0, Direction.WEST), (2, 3))
        assert(path == (2, 1) :: (3, 1) :: (3, 2) :: (3, 3) :: (2, 1) :: Nil || path == (2, 1) :: (1, 1) :: (1, 2) :: (1, 3) :: (2, 3) :: Nil)
      }
    }
    "calculate the desired direction" when {
      "target is in front of the ghost" in {
        var blinky = Ghost(GhostType.BLINKY, ORIGIN, 1.0, Direction.NORTH)
        var pacman = Pacman(ORIGIN + Point2D(0, TileGeography.SIZE), 1.0, Direction.SOUTH)
        assert(GhostAI.desiredDirection(blinky, pacman) == Direction.SOUTH)

        blinky = Ghost(GhostType.BLINKY, ORIGIN + Point2D(0, TileGeography.SIZE), 1.0, Direction.NORTH)
        pacman = Pacman(ORIGIN, 1.0, Direction.SOUTH)
        assert(GhostAI.desiredDirection(blinky, pacman) == Direction.NORTH)
      }
      "target is after the tunnel" in {
        val blinky = Ghost(GhostType.BLINKY, ORIGIN + Point2D(0, TileGeography.SIZE), 1.0, Direction.EAST)
        val pacman = Pacman(ORIGIN + Point2D(TileGeography.SIZE * 2, TileGeography.SIZE), 1.0, Direction.WEST)
        assert(GhostAI.desiredDirection(blinky, pacman) == Direction.WEST)
      }
      "target is behind a wall" in {
        val blinky = Ghost(GhostType.BLINKY, ORIGIN + Point2D(TileGeography.SIZE, 0), 1.0, Direction.EAST)
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
    "calculate the shortest path on classic map" when {
      "in any condition" in {
        // scalastyle:off magic.number
        var classicPath = GhostAI.shortestPathClassic((24, 26), (15, 29))
        assert(classicPath == (24, 26) :: (15, 29) :: Nil)

        classicPath = GhostAI.shortestPathClassic((1, 5), (26, 5))
        assert(classicPath == (1, 5) :: (6, 5) :: (9, 5) :: (12, 5) :: (15, 5) :: (18, 5) :: (21, 5) :: (26, 5) :: Nil)

        classicPath = GhostAI.shortestPathClassic((6, 1), (6, 23))
        assert(classicPath == (6, 1) :: (6, 5) :: (6, 8) :: (6, 14) :: (6, 20) :: (6, 23) :: Nil)

        classicPath = GhostAI.shortestPathClassic((1, 5), (18, 17))
        assert(classicPath == (1, 5) :: (6, 5) :: (9, 5) :: (12, 11) :: (13,11) :: (14,11) :: (15, 11) :: (18, 14) :: (18, 17) :: Nil)

        classicPath = GhostAI.shortestPathClassic((13, 13), (12, 11))
        assert(classicPath == (13,13) :: (13,12) :: (13,11) :: (12,11) :: Nil)

        classicPath = GhostAI.shortestPathClassic((13, 15), (12, 11))
        assert(classicPath == (13,15) :: (13,14) :: (13,13) :: (13,12) :: (13,11) :: (12,11) :: Nil)

        classicPath = GhostAI.shortestPathClassic((15, 14), (12, 11))
        assert(classicPath == (15, 14) :: (14, 14) :: (14,13) :: (14,12) :: (14,11) :: (13,11) :: (12,11) :: Nil)

        classicPath = GhostAI.shortestPathClassic((11, 14), (12, 11))
        assert(classicPath == (11, 14) :: (12,14) :: (13,14) :: (13,13) :: (13,12) :: (13,11) :: (12,11) :: Nil)
        // scalastyle:on magic.number
      }
      "calculate the desired direction" when {
        "in any condition" in {
          // scalastyle:off magic.number
          var ghost = Ghost(GhostType.INKY, Point2D(12 * TileGeography.SIZE, 29 * TileGeography.SIZE), 1.0, Direction.NORTH)
          assert(GhostAI.desiredDirectionClassic(ghost, (15, 29)).contains(Direction.EAST))

          ghost = Ghost(GhostType.INKY, Point2D(24 * TileGeography.SIZE, 26 * TileGeography.SIZE), 1.0, Direction.SOUTH)
          assert(GhostAI.desiredDirectionClassic(ghost, (15, 29)).contains(Direction.EAST))
          assert(GhostAI.desiredDirectionClassic(ghost, (15, 29)).contains(Direction.EAST))
          assert(GhostAI.desiredDirectionClassic(ghost, (21, 23)).contains(Direction.WEST))
          assert(GhostAI.desiredDirectionClassic(ghost, (15, 11)).contains(Direction.WEST))

          ghost = Ghost(GhostType.INKY, Point2D(13 * TileGeography.SIZE, 15 * TileGeography.SIZE), 1.0, Direction.NORTH)
          assert(GhostAI.desiredDirectionClassic(ghost, (12, 11)).contains(Direction.NORTH))
          // scalastyle:on magic.number
        }
      }
      "find the best direction for all situation" when {
        "faraway from pacman" in {
          val ghost = Ghost(GhostType.INKY, Point2D(6 * TileGeography.SIZE, 23 * TileGeography.SIZE), 1.0, Direction.NORTH)
          val pacman = Pacman(Point2D(6 * TileGeography.SIZE, 1 * TileGeography.SIZE), 1.0, Direction.SOUTH)
          assert(GhostAI.calculateDirectionClassic(ghost, pacman).contains(Direction.NORTH))
        }
        "the ghost's tile isn't a crossing" in {
          var ghost = Ghost(GhostType.INKY, Point2D(4 * TileGeography.SIZE, 26 * TileGeography.SIZE), 1.0, Direction.NORTH)
          val pacman = Pacman(Point2D(6 * TileGeography.SIZE, 1 * TileGeography.SIZE), 1.0, Direction.SOUTH)
          assert(GhostAI.calculateDirectionClassic(ghost, pacman).contains(Direction.EAST))

          ghost = ghost.copy(position = Point2D(3 * TileGeography.SIZE, 25 * TileGeography.SIZE))
          assert(GhostAI.calculateDirectionClassic(ghost, pacman).contains(Direction.WEST))
        }
        "pacman is near" in {
          val ghost = Ghost(GhostType.INKY, Point2D(21 * TileGeography.SIZE, 8 * TileGeography.SIZE), 1.0, Direction.NORTH)
          var pacman = Pacman(Point2D(26 * TileGeography.SIZE, 6 * TileGeography.SIZE), 1.0, Direction.NORTH)
          assert(GhostAI.calculateDirectionClassic(ghost, pacman).contains(Direction.EAST))

          pacman = pacman.copy(direction = Direction.SOUTH)
          assert(GhostAI.calculateDirectionClassic(ghost, pacman).contains(Direction.EAST))
        }
      }
    }
  }
}
