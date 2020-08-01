package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.math.{Point2D, TileGeography, Vector2D}
import it.unibo.scalapacman.lib.model.{Character, Eatable, Ghost, Map, Pacman, Tile}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.Direction.{EAST, NORTH, SOUTH, WEST}
import it.unibo.scalapacman.lib.engine.CircularMovement.{moveFor, moveUntil}
import it.unibo.scalapacman.lib.model.Map.MapIndexes
import it.unibo.scalapacman.lib.model.Tile.Track

import scala.reflect.ClassTag

object GameHelpers {

  implicit class CharacterHelper(character: Character)(implicit map: Map) {
    def changePosition(position: Point2D): Character = character match {
      case ghost: Ghost => ghost.copy(position = position)
      case pacman: Pacman => pacman.copy(position = position)
      case _ => throw new IllegalArgumentException("Unknown character type")
    }

    def changeDirection(direction: Direction): Character = character match {
      case ghost: Ghost => ghost.copy(direction = direction)
      case pacman: Pacman => pacman.copy(direction = direction)
      case _ => throw new IllegalArgumentException("Unknown character type")
    }

    def revert: Character = character.direction match {
      case EAST => changeDirection(WEST)
      case WEST => changeDirection(EAST)
      case NORTH => changeDirection(SOUTH)
      case SOUTH => changeDirection(NORTH)
      case _ => character
    }

    def desireRevert(desiredDirection: Direction): Boolean = character.direction match {
      case EAST if desiredDirection == WEST => true
      case WEST if desiredDirection == EAST => true
      case NORTH if desiredDirection == SOUTH => true
      case SOUTH if desiredDirection == NORTH => true
      case _ => false
    }

    def nextTileCenter(implicit map: Map): Point2D =
      (tileOrigin :: nextTileOrigin :: Nil)
        .map(_ + TileGeography.center)
        .minBy(moveUntil(character, _))

    def changeDirectionIfPossible(desiredDirection: Direction)(implicit map: Map): Character =
      if (character.direction != desiredDirection && nextTile(desiredDirection).walkable(character)) {
        changeDirection(desiredDirection)
      } else {
        character
      }

    def moveIfPossible(timeMs: Double)(implicit map: Map): Character = if (nextTile.walkable(character)) {
      changePosition(moveFor(character, timeMs))
    } else {
      character
    }

    def tileOrigin: Point2D = map.tileOrigin(character.position)

    def nextTileOrigin: Point2D = map.tileOrigin(character.position, Some(character.direction).map(CharacterMovement.vector))

    def tile: Tile = map.tile(character.position)

    def nextTile: Tile = map.tile(character.position, Some(character.direction).map(CharacterMovement.vector))

    def nextTile(direction: Direction): Tile = map.tile(character.position, Some(direction).map(CharacterMovement.vector))

    def tileIndexes: MapIndexes = map.tileIndexes(character.position)

    def eat: Map = map.empty(character.tileIndexes)
  }

  implicit class MapHelper(map: Map) {
    val height: Int = map.tiles.size
    val width: Int = map.tiles.head.size

    private def tileIndex(x: Double, watchOut: Option[Double] = None): Int = ((x + watchOut.getOrElse(0.0)) / TileGeography.SIZE).floor.toInt

    def tile(position: Point2D, watchOut: Option[Vector2D] = None): Tile =
      map.tiles(pacmanEffect(tileIndex(position.y, watchOut.map(_.y)), height))(pacmanEffect(tileIndex(position.x, watchOut.map(_.x)), width))

    def tile(indexes: MapIndexes): Tile = map.tiles(pacmanEffect(indexes._2, height))(pacmanEffect(indexes._1, width))

    def tileIndexes(position: Point2D): MapIndexes = (
      pacmanEffect(tileIndex(position.x), width),
      pacmanEffect(tileIndex(position.y), height)
    )

    def tileIndexes(indexes: MapIndexes): MapIndexes = (
      pacmanEffect(indexes._1, width),
      pacmanEffect(indexes._2, height)
    )

    def tileOrigin(position: Point2D, watchOut: Option[Vector2D] = None): Point2D = Point2D(
      pacmanEffect(tileIndex(position.x, watchOut.map(_.x)), width) * TileGeography.SIZE,
      pacmanEffect(tileIndex(position.y, watchOut.map(_.y)), height) * TileGeography.SIZE
    )

    def tileOrigin(indexes: MapIndexes): Point2D = Point2D(
      pacmanEffect(indexes._1, width) * TileGeography.SIZE,
      pacmanEffect(indexes._2, height) * TileGeography.SIZE
    )

    @scala.annotation.tailrec
    private def pacmanEffect(x: Int, max: Int): Int = x match {
      case x: Int if x >= 0 => x % max
      case x: Int => pacmanEffect(x + max, max)
    }

    def empty(indexes: MapIndexes): Map = putEatable(indexes, None)

    def putEatable(indexes: MapIndexes, option: Option[Eatable]): Map = map.copy(
      tiles = map.tiles.updated(indexes._2, putEatableOnRow(indexes._1, map.tiles(indexes._2), option))
    )

    private def putEatableOnRow(index: Int, row: List[Tile], option: Option[Eatable]): List[Tile] =
      row.updated(index, putEatableOnTile(row(index), option))

    private def putEatableOnTile(tile: Tile, option: Option[Eatable]): Tile = tile match {
      case Track(_) => Track(option)
      case t: Tile => t
    }

    def eatablesToList[A <: Eatable : ClassTag]: Seq[(MapIndexes, A)] =
      for (
        y <- 0 until map.height;
        x <- 0 until map.width;
        eatable <- map.tiles(y)(x).eatable collect { case a: A => a }
      ) yield ((x, y), eatable)
  }
}
