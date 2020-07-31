package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.math.{Point2D, TileGeography}
import it.unibo.scalapacman.lib.model.{Direction, Map, Pacman, Tile}
import it.unibo.scalapacman.lib.engine.CircularMovement.{moveFor, moveUntil}
import org.scalatest.wordspec.AnyWordSpec

class CircularMovementTest extends AnyWordSpec {
  val MAP_WIDTH = 3
  val MAP_HEIGHT = 4
  val TIME_MS = 1
  implicit val MAP: Map = Map(
    List.fill(MAP_HEIGHT)(
      List.fill(MAP_WIDTH)(Tile.Track(None))
    )
  )

  "With circular movement, a character" when {
    "it's moving by time" can {
      "exit east and reappear west" in {
        val character = Pacman(
          Point2D(TileGeography.SIZE * MAP_WIDTH, 0),
          TileGeography.SIZE,
          Direction.EAST
        )
        val arrival: Point2D = moveFor(character, TIME_MS)
        assert(arrival.x == TileGeography.SIZE)
      }
      "exit west and reappear east" in {
        val character = Pacman(
          Point2D(0, 0),
          TileGeography.SIZE,
          Direction.WEST
        )
        val arrival: Point2D = moveFor(character, TIME_MS)
        assert(arrival.x == TileGeography.SIZE * (MAP_WIDTH - 1))
      }
      "exit north and reappear south" in {
        val character = Pacman(
          Point2D(0, 0),
          TileGeography.SIZE,
          Direction.NORTH
        )
        val arrival: Point2D = moveFor(character, TIME_MS)
        assert(arrival.y == TileGeography.SIZE * (MAP_HEIGHT - 1))
      }
      "exit south and reappear north" in {
        val character = Pacman(
          Point2D(0, TileGeography.SIZE * MAP_HEIGHT),
          TileGeography.SIZE,
          Direction.SOUTH
        )
        val arrival: Point2D = moveFor(character, TIME_MS)
        assert(arrival.y == TileGeography.SIZE)
      }
    }

    "it's moving by target" can {
      "exit east and reappear west" in {
        val character = Pacman(
          Point2D(TileGeography.SIZE * MAP_WIDTH, 0),
          TileGeography.SIZE,
          Direction.EAST
        )
        assertResult(TIME_MS)(moveUntil(character, Point2D(TileGeography.SIZE, 0)))
      }
      "exit west and reappear east" in {
        val character = Pacman(
          Point2D(0, 0),
          TileGeography.SIZE,
          Direction.WEST
        )
        assertResult(TIME_MS)(moveUntil(character, Point2D(TileGeography.SIZE * (MAP_WIDTH - 1), 0)))
      }
      "exit north and reappear south" in {
        val character = Pacman(
          Point2D(0, 0),
          TileGeography.SIZE,
          Direction.NORTH
        )
        assertResult(TIME_MS)(moveUntil(character, Point2D(0, TileGeography.SIZE * (MAP_HEIGHT - 1))))
      }
      "exit south and reappear north" in {
        val character = Pacman(
          Point2D(0, TileGeography.SIZE * MAP_HEIGHT),
          TileGeography.SIZE,
          Direction.SOUTH
        )
        assertResult(TIME_MS)(moveUntil(character, Point2D(0, TileGeography.SIZE)))
      }
    }
  }
}
