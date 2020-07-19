package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.math.{Point2D, TileGeography, Vector2D}
import it.unibo.scalapacman.lib.model.{Character, Direction, Ghost, Map, Pacman}
import it.unibo.scalapacman.lib.model.Direction.{EAST, NORTH, SOUTH, WEST}
import it.unibo.scalapacman.lib.engine.CircularMovement.{moveFor, moveUntil}
import it.unibo.scalapacman.lib.model.Tile.Tile

object GameHelpers {

  implicit class CharacterHelper(character: Character)(implicit map: Map) {
    def changePosition(position: Point2D): Character = character match {
      case ghost: Ghost => Ghost(ghost.ghostType, position, character.speed, character.direction)
      case _ => Pacman(position, character.speed, character.direction)
    }

    def changeDirection(direction: Direction): Character = character match {
      case ghost: Ghost => Ghost(ghost.ghostType, character.position, character.speed, direction)
      case _ => Pacman(character.position, character.speed, direction)
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

    def tileOrigin: Point2D = map.tileOrigin(character.position, None)

    def nextTileOrigin: Point2D = map.tileOrigin(character.position, Some(character.direction).map(CharacterMovement.unitVector))

    def tile: Tile = map.tile(character.position, None)

    def nextTile: Tile = map.tile(character.position, Some(character.direction).map(CharacterMovement.unitVector))

    def nextTile(direction: Direction): Tile = map.tile(character.position, Some(direction).map(CharacterMovement.unitVector))
  }

  implicit class MapHelper(map: Map) {
    val width: Int = map.tiles.size
    val height: Int = map.tiles.head.size

    private def tileIndex(x: Double, watchOut: Option[Double]): Int = (x / TileGeography.SIZE + watchOut.getOrElse(0.0)).floor.toInt

    def tile(position: Point2D, watchOut: Option[Vector2D]): Tile =
      map.tiles(pacmanEffect(tileIndex(position.y, watchOut.map(_.y)), height))(pacmanEffect(tileIndex(position.x, watchOut.map(_.x)), width))

    @scala.annotation.tailrec
    private def pacmanEffect(x: Int, max: Int): Int = x match {
      case x: Int if x > 0 => x % max
      case x: Int => pacmanEffect(x + max, max)
    }

    def tileOrigin(position: Point2D, watchOut: Option[Vector2D]): Point2D = Point2D(
      pacmanEffect(tileIndex(position.x, watchOut.map(_.x)), width) * TileGeography.SIZE,
      pacmanEffect(tileIndex(position.y, watchOut.map(_.y)), height) * TileGeography.SIZE
    )
  }
}
