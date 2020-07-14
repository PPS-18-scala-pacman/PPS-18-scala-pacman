package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.math.{Point2D, TileGeography, Vector2D}
import it.unibo.scalapacman.lib.model.{Character, Direction, Ghost, Map, Pacman}
import it.unibo.scalapacman.lib.model.Tile.Tile
import it.unibo.scalapacman.lib.engine.CircularMovement.{moveFor, moveUntil}
import it.unibo.scalapacman.lib.model.Direction.{EAST, NORTH, SOUTH, WEST}

object GameMovement {

  /**
   * 1. Desidero invertire la direzione?
   * - se SI Inverto la rotta
   * 2. Sono al centro della tile?
   * - se NO Mi muovo fino al centro (jump 2)
   * 3. Voglio curvare e la prossima Tile è agibile?
   * - se SI Cambio la direzione
   * 4. La prossima tile è agibile?
   * - se SI finisco di muovermi
   *
   * @param character Character to move
   * @param timeMs Time available
   * @param desiredDirection Direction to turn, if possible
   * @param map Map of the game
   * @return The updated character
   */
  @scala.annotation.tailrec
  def move(character: Character, timeMs: Double, desiredDirection: Direction)(implicit map: Map): Character = (character, timeMs, desiredDirection) match {
    case (_, 0, _) => character
    case _ if character desireRevert desiredDirection => move(character revert, timeMs, desiredDirection)
    case _ if character.position == character.nextTileCenter => character.changeDirectionIfPossible(desiredDirection).moveIfPossible(timeMs)
    case _ if moveUntil(character, character.nextTileCenter) > timeMs => character.move(moveFor(character, timeMs))
    case _ => move(character.move(character.nextTileCenter), timeMs - moveUntil(character, character.nextTileCenter), desiredDirection)
  }

  implicit private class CharacterEnhanced(character: Character) {
    def move(position: Point2D): Character = character match {
      case ghost: Ghost => Ghost(ghost.ghostType, position, character.speed, character.direction)
      case _ => Pacman(position, character.speed, character.direction)
    }

    def changeDirection(direction: Direction): Character = character match {
      case ghost: Ghost => Ghost(ghost.ghostType, character.position, character.speed, direction)
      case _ => Pacman(character.position, character.speed, direction)
    }

    //    def revert(desiredDirection: Direction): Character = character.direction match {
    //      case direction: Direction if direction == desiredDirection => character
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
      (map.tileOrigin(character) :: map.nextTileOrigin(character) :: Nil)
        .map(_ + TileGeography.center)
        .minBy(center => moveUntil(character, center))

    def changeDirectionIfPossible(desiredDirection: Direction)(implicit map: Map): Character =
      if (character.direction != desiredDirection && map.nextTile(character, desiredDirection).walkable(character)) {
        changeDirection(desiredDirection)
      } else {
        character
      }

    def moveIfPossible(timeMs: Double)(implicit map: Map): Character = if (map.nextTile(character).walkable(character)) {
      character.move(moveFor(character, timeMs))
    } else {
      character
    }
  }

  implicit private class MapEnhanced(map: Map) {
    val width: Int = map.tiles.size
    val height: Int = map.tiles.head.size

    private def tileIndex(x: Double, watchOut: Option[Double]): Int = (x / TileGeography.SIZE + watchOut.getOrElse(0.0)).floor.toInt

    private def tile(position: Point2D, watchOut: Option[Vector2D]) =
      map.tiles(pacmanEffect(tileIndex(position.y, watchOut.map(_.y)), height))(pacmanEffect(tileIndex(position.x, watchOut.map(_.x)), width))

    def tile(character: Character): Tile = tile(character.position, None)

    def nextTile(character: Character): Tile = tile(character.position, Some(character.direction).map(CharacterMovement.unitVector))

    def nextTile(character: Character, direction: Direction): Tile = tile(character.position, Some(direction).map(CharacterMovement.unitVector))

    private def tileOrigin(position: Point2D, watchOut: Option[Vector2D]) = Point2D(
      pacmanEffect(tileIndex(position.x, watchOut.map(_.x)), width) * TileGeography.SIZE,
      pacmanEffect(tileIndex(position.y, watchOut.map(_.y)), height) * TileGeography.SIZE
    )

    def tileOrigin(character: Character): Point2D =
      tileOrigin(character.position, None)

    def nextTileOrigin(character: Character): Point2D =
      tileOrigin(character.position, Some(character.direction).map(CharacterMovement.unitVector))

    @scala.annotation.tailrec
    private def pacmanEffect(x: Int, max: Int): Int = x match {
      case x: Int if x > 0 => x % max
      case x: Int => pacmanEffect(x + max, max)
    }
  }

}
