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
   * 2. Sono al centro?
   * - se NO Mi muovo fino al centro (jump 2)
   * 3. Voglio curvare e la prossima Tile è agibile?
   * - se SI Cambio la direzione
   * 4. La prossima tile è agibile?
   * - se SI finisco di muovermi
   *
   * @param map
   * @param character
   * @param timeMs
   * @param desiredDirection
   * @return
   */
  def move(map: Map, character: Character, timeMs: Double, desiredDirection: Direction): Character = {
    def nextTileCenter: Point2D =
      (map.tileOrigin(character) :: map.nextTileOrigin(character) :: Nil)
        .map(_ + TileGeography.center)
        .minBy(center => moveUntil(map, character, center))

    def characterDesireRevert: Boolean = character.direction match {
      case EAST if desiredDirection == WEST => true
      case WEST if desiredDirection == EAST => true
      case NORTH if desiredDirection == SOUTH => true
      case SOUTH if desiredDirection == NORTH => true
      case _ => false
    }

    if (timeMs == 0) return character
    val character1: Character = if (characterDesireRevert) character revert else character
    val character2: Character = if (character.position == nextTileCenter) character1
      else if (moveUntil(map, character1, nextTileCenter) > timeMs) return character1.move(moveFor(map, character1, timeMs)) // BREAK
      else return move(map, character1.move(nextTileCenter), timeMs - moveUntil(map, character1, nextTileCenter), desiredDirection) // BREAK

    val character3: Character = if (character2.direction != desiredDirection && map.nextTile(character2, desiredDirection).walkable(character2)) character2.changeDirection(desiredDirection) else character2
    val character4: Character = if (map.nextTile(character3).walkable(character3)) character3.move(moveFor(map, character3, timeMs)) else character3
    return character4
  }

  //    1. revert(character)
  //    2. character.position match { case position if position == tile(position).center => Unit; case position if moveUntil() > timeMs => moveFor(); case position => move(map, character.move(tile(position).center), timeMs - moveUntil(), desiredDirection) }
  //    3. if (desireToCurve(character) && isAgibile(nextTile(desiredDirection))) changeDirection(character)
  //    4. if (isAgibile(nextTile())) moveFor()

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

    private def tileOrigin(position: Point2D, watchOut: Option[Vector2D]) =
      Point2D((pacmanEffect(tileIndex(position.x, watchOut.map(_.x)), width)) * TileGeography.SIZE, (pacmanEffect(tileIndex(position.y, watchOut.map(_.y)), height)) * TileGeography.SIZE)

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
