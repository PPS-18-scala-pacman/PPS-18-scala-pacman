package it.unibo.scalapacman.lib.engine

import org.scalatest.wordspec.AnyWordSpec
import it.unibo.scalapacman.lib.model.{Direction, Ghost, Map, Pacman, Tile}
import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper, MapHelper}
import it.unibo.scalapacman.lib.math.{Point2D, TileGeography, Vector2D}

class GameHelpersTest extends AnyWordSpec {
  val MAP_WIDTH = 3
  val MAP_HEIGHT = 4
  val TIME_MS = 1
  implicit val MAP: Map = Map(
    List(
      List.fill(MAP_WIDTH)(Tile.Track(None)),
      List.fill(MAP_WIDTH)(Tile.Wall()),
      List.fill(MAP_WIDTH)(Tile.Track(None)),
      List.fill(MAP_WIDTH)(Tile.Track(None))
    )
  )
  val PACMAN: Pacman = Pacman(Point2D(0, 0), 1.0, Direction.EAST)
  val GHOST: Ghost = Ghost.inky(Point2D(0, 0), 1.0, Direction.EAST)

  "Game helpers" should {
    "contains a map helper with pacman effect" which {
      "provide map width" in {
        assertResult(MAP_WIDTH)(MAP.width)
      }
      "provide map height" in {
        assertResult(MAP_HEIGHT)(MAP.height)
      }
      "calculate a tile" in {
        // From indexes
        assertResult(MAP.tiles(2).head)(MAP.tile((0, 2)))
        assertResult(MAP.tiles.head.head)(MAP.tile((MAP_WIDTH, MAP_HEIGHT)))
        assertResult(MAP.tiles(1)(1))(MAP.tile((MAP_WIDTH + 1, MAP_HEIGHT + 1)))

        // From position
        assertResult(MAP.tiles(2).head)(MAP.tile(Point2D(0 * TileGeography.SIZE, 2 * TileGeography.SIZE), None))
        assertResult(MAP.tiles.head.head)(MAP.tile(Point2D(MAP_WIDTH * TileGeography.SIZE, MAP_HEIGHT * TileGeography.SIZE), None))
        assertResult(MAP.tiles(1)(1))(MAP.tile(Point2D((MAP_WIDTH + 1) * TileGeography.SIZE, (MAP_HEIGHT + 1) * TileGeography.SIZE), None))

        // From position with watchOut
        assertResult(MAP.tiles(3).head)(
          MAP.tile(Point2D(0, 2 * TileGeography.SIZE), Some(Vector2D(0, TileGeography.SIZE)))
        )
        assertResult(MAP.tiles(1)(1))(
          MAP.tile(Point2D(MAP_WIDTH * TileGeography.SIZE, MAP_HEIGHT * TileGeography.SIZE), Some(Vector2D(TileGeography.SIZE, TileGeography.SIZE)))
        )
        assertResult(MAP.tiles.last.last)(
          MAP.tile(Point2D(MAP_WIDTH * TileGeography.SIZE, MAP_HEIGHT * TileGeography.SIZE), Some(Vector2D(-TileGeography.SIZE, -TileGeography.SIZE)))
        )
      }
      "calculate a tile origin" in {
        // without watch out
        assertResult(Point2D(0, 0))(MAP.tileOrigin(Point2D(0, 0), None))
        assertResult(Point2D(0, 0))(MAP.tileOrigin(Point2D(MAP_WIDTH * TileGeography.SIZE, MAP_HEIGHT * TileGeography.SIZE), None))
        assertResult(Point2D(0, TileGeography.SIZE))(MAP.tileOrigin(Point2D(MAP_WIDTH * TileGeography.SIZE, (MAP_HEIGHT + 1) * TileGeography.SIZE), None))

        // with watch out
        val halfWatchOut = Some(Vector2D(TileGeography.SIZE / 2, TileGeography.SIZE / 2))
        val fullWatchOut = Some(Vector2D(TileGeography.SIZE, TileGeography.SIZE))
        assertResult(Point2D(0, 0))(MAP.tileOrigin(Point2D(0, 0), halfWatchOut))
        assertResult(Point2D(TileGeography.SIZE, TileGeography.SIZE))(MAP.tileOrigin(Point2D(0, 0), fullWatchOut))
        assertResult(Point2D(0, 0))(MAP.tileOrigin(Point2D(MAP_WIDTH * TileGeography.SIZE, MAP_HEIGHT * TileGeography.SIZE), halfWatchOut))
        assertResult(Point2D(TileGeography.SIZE, TileGeography.SIZE))(
          MAP.tileOrigin(Point2D(MAP_WIDTH * TileGeography.SIZE, MAP_HEIGHT * TileGeography.SIZE), fullWatchOut)
        )
      }
      "calculate a tile indexes" in {
        // from point2D
        assertResult((0, 0))(MAP.tileIndexes(Point2D(0, 0)))
        assertResult((MAP_WIDTH - 1, MAP_HEIGHT - 1))(MAP.tileIndexes(Point2D(-1, -1)))
        assertResult((0, 0))(MAP.tileIndexes(Point2D(TileGeography.SIZE - 1, TileGeography.SIZE - 1)))
        assertResult((1, 1))(MAP.tileIndexes(Point2D(TileGeography.SIZE, TileGeography.SIZE)))
        assertResult((0, 0))(MAP.tileIndexes(Point2D(MAP_WIDTH, 0)))
        assertResult((0, 0))(MAP.tileIndexes(Point2D(0, MAP_HEIGHT)))

        // from indexes
        assertResult((0, 0))(MAP.tileIndexes((0, 0)))
        assertResult((MAP_WIDTH - 1, MAP_HEIGHT - 1))(MAP.tileIndexes((-1, -1)))
        assertResult((1, 1))(MAP.tileIndexes((1, 1)))
        assertResult((0, 0))(MAP.tileIndexes((MAP_WIDTH, 0)))
        assertResult((0, 0))(MAP.tileIndexes((0, MAP_HEIGHT)))
      }
    }
    "contains a character helper" which {
      "move the character if possible" in {
        assertResult(PACMAN.copy(position = PACMAN.position + Point2D(1, 0)))(PACMAN.moveIfPossible(1))
        assertResult(PACMAN.copy(direction = Direction.SOUTH))(PACMAN.copy(direction = Direction.SOUTH).moveIfPossible(1))
      }
      "change character position" in {
        assertResult(PACMAN.copy(position = Point2D(17.3, 9)))(PACMAN.changePosition(Point2D(17.3, 9))) // scalastyle:ignore magic.number
        assertResult(GHOST.copy(position = Point2D(17.3, 9)))(GHOST.changePosition(Point2D(17.3, 9))) // scalastyle:ignore magic.number
      }
      "change character direction" when {
        "always" in {
          assertResult(PACMAN.copy(direction = Direction.NORTH))(PACMAN.changeDirection(Direction.NORTH))
          assertResult(GHOST.copy(direction = Direction.NORTH))(GHOST.changeDirection(Direction.NORTH))
        }
        "is possible" in {
          assertResult(PACMAN.copy(direction = Direction.NORTH))(PACMAN.changeDirectionIfPossible(Direction.NORTH))
          assertResult(PACMAN)(PACMAN.changeDirectionIfPossible(Direction.SOUTH))
        }
      }
      "check if the character desire to revert" in {
        assertResult(true)(PACMAN.desireRevert(Direction.WEST))
        assertResult(false)(PACMAN.desireRevert(Direction.SOUTH))
        assertResult(false)(PACMAN.desireRevert(Direction.EAST))
      }
      "revert character direction" in {
        assertResult(PACMAN.copy(direction = Direction.WEST))(PACMAN.revert)
      }
      "calculate the next tile center" in {
        assertResult(Point2D(3.5, 4.5))(PACMAN.nextTileCenter) // scalastyle:ignore magic.number
        assertResult(Point2D(3.5, 4.5) + Point2D(TileGeography.SIZE, TileGeography.SIZE))( // scalastyle:ignore magic.number
          PACMAN.copy(position = PACMAN.position + Point2D(TileGeography.SIZE, TileGeography.SIZE)).nextTileCenter
        )
      }
      "provide current tile" in {
        assertResult(MAP.tiles(2).head)(PACMAN.tile)
        assertResult(MAP.tiles.head.head)(PACMAN.copy(position = Point2D(MAP_WIDTH * TileGeography.SIZE, MAP_HEIGHT * TileGeography.SIZE)).tile)
        assertResult(MAP.tiles(1)(1))(PACMAN.copy(position = Point2D((MAP_WIDTH + 1) * TileGeography.SIZE, (MAP_HEIGHT + 1) * TileGeography.SIZE)).tile)
      }
      "provide current tile origin" in {
        assertResult(Point2D(0, 0))(PACMAN.tileOrigin)
        assertResult(Point2D(0, 0))(PACMAN.copy(position = Point2D(MAP_WIDTH * TileGeography.SIZE, MAP_HEIGHT * TileGeography.SIZE)).tileOrigin)
        assertResult(Point2D(0, TileGeography.SIZE))(
          PACMAN.copy(position = Point2D(MAP_WIDTH * TileGeography.SIZE, (MAP_HEIGHT + 1) * TileGeography.SIZE)).tileOrigin
        )
      }
      "provide current tile indexes" in {
        assertResult((0, 0))(PACMAN.tileIndexes)
        assertResult((MAP_WIDTH - 1, MAP_HEIGHT - 1))(PACMAN.copy(position = Point2D(-1, -1)).tileIndexes)
        assertResult((0, 0))(PACMAN.copy(position = Point2D(TileGeography.SIZE - 1, TileGeography.SIZE - 1)).tileIndexes)
        assertResult((1, 1))(PACMAN.copy(position = Point2D(TileGeography.SIZE, TileGeography.SIZE)).tileIndexes)
        assertResult((0, 0))(PACMAN.copy(position = Point2D(MAP_WIDTH, 0)).tileIndexes)
        assertResult((0, 0))(PACMAN.copy(position = Point2D(0, MAP_HEIGHT)).tileIndexes)
      }
      "provide next tile" in {
        assertResult(MAP.tiles(3).head)(
          PACMAN.copy(position = Point2D(0, 2 * TileGeography.SIZE), direction = Direction.SOUTH).nextTile
        )
        assertResult(MAP.tiles(1)(1))(
          PACMAN.copy(position = Point2D(MAP_WIDTH * TileGeography.SIZE, MAP_HEIGHT * TileGeography.SIZE), direction = Direction.SOUTHEAST).nextTile
        )
        assertResult(MAP.tiles.last.last)(
          PACMAN.copy(position = Point2D(MAP_WIDTH * TileGeography.SIZE, MAP_HEIGHT * TileGeography.SIZE), direction = Direction.NORTHWEST).nextTile
        )
      }
      "provide next tile origin" in {
        assertResult(Point2D(TileGeography.SIZE, 0))(PACMAN.nextTileOrigin)
        assertResult(Point2D(0, 0))(
          PACMAN.copy(position = Point2D(MAP_WIDTH * TileGeography.SIZE, MAP_HEIGHT * TileGeography.SIZE)).tileOrigin
        )
      }
      "provide next tile in a custom direction" in {
        assertResult(MAP.tiles(3).head)(
          PACMAN.copy(position = Point2D(0, 2 * TileGeography.SIZE)).nextTile(Direction.SOUTH)
        )
        assertResult(MAP.tiles(1)(1))(
          PACMAN.copy(position = Point2D(MAP_WIDTH * TileGeography.SIZE, MAP_HEIGHT * TileGeography.SIZE)).nextTile(Direction.SOUTHEAST)
        )
        assertResult(MAP.tiles.last.last)(
          PACMAN.copy(position = Point2D(MAP_WIDTH * TileGeography.SIZE, MAP_HEIGHT * TileGeography.SIZE)).nextTile(Direction.NORTHWEST)
        )
      }
    }
  }

}
