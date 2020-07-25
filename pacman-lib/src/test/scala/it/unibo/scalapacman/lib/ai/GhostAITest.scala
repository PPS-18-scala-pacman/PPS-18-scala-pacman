package it.unibo.scalapacman.lib.ai

import it.unibo.scalapacman.lib.model.{Direction, Map, Pacman}
import it.unibo.scalapacman.lib.ai.GhostAI.engine
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
    "work" in {
      // One step
      assert(GhostAI.shortestPath(Pacman(ORIGIN, 1.0, Direction.WEST), (2, 1)) == (1, 1) :: (2, 1) :: Nil)
      // Pacman effect
      assert(GhostAI.shortestPath(Pacman(ORIGIN + Point2D(0, TileGeography.SIZE), 1.0, Direction.WEST), (3, 2)) == (1, 2) :: (0, 2) :: (4, 2) :: (3, 2) :: Nil)
      // Unwalkable tiles
      val path = GhostAI.shortestPath(Pacman(ORIGIN + Point2D(TileGeography.SIZE, 0), 1.0, Direction.WEST), (2, 3))
      assert(path == (2, 1) :: (3, 1) :: (3, 2) :: (3, 3) :: (2, 1) :: Nil || path == (2, 1) :: (1, 1) :: (1, 2) :: (1, 3) :: (2, 3) :: Nil)
    }
  }
}
