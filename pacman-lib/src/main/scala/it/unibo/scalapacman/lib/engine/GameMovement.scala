package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.math.{Point2D, TileGeography, Vector2D}
import it.unibo.scalapacman.lib.model.{Character, Direction, Ghost, Map, Pacman}
import it.unibo.scalapacman.lib.model.Tile.Tile
import it.unibo.scalapacman.lib.engine.CircularMovement.{moveFor, moveUntil}
import it.unibo.scalapacman.lib.model.Direction.{EAST, NORTH, SOUTH, WEST}

object GameMovement {

  /**
   * Funzione di movimento del personaggio.
   *
   * Di seguito l'algoritmo implementato:
   * 1. Il personaggio ha terminato il tempo a disposizione per muoversi?
   * -> Termina il movimento
   * -> Riparte dal punto 1
   * 2. Il personaggio desidera invertire la direzione?
   * -> Inverte la direzione
   * 3. Il personaggio si trova al centro della tile corrente?
   * -> Se lo desidera ed è possibile, curva cambiando direzione
   * -> Se la prossima tile è camminabile dal personaggio, si sposta per tutto il tempo rimasto a disposizione
   * 4. Il personaggio non possiede abbastanza tempo da raggiungere il prossimo centro di una tile?
   * -> Si sposta per tutto il tempo rimasto a disposizione
   * 5. Altrimenti il personaggio è in grado di raggiungere il prossimo centro senza consumare tutto il tempo a disposizione
   * -> Si sposta fino al successivo centro di una tile
   * -> Riparte dal punto 1
   *
   * @param character        Character to move
   * @param timeMs           Time available
   * @param desiredDirection Desired direction, to change if and when possible
   * @param map              Map of the game
   * @return The updated character
   */
  @scala.annotation.tailrec
  def move(character: Character, timeMs: Double, desiredDirection: Direction)(implicit map: Map): Character = (character, timeMs, desiredDirection) match {
    case (_, 0, _) => character
    case _ if character desireRevert desiredDirection => move(character revert, timeMs, desiredDirection)
    case _ if character.position == character.nextTileCenter => (character changeDirectionIfPossible desiredDirection) moveIfPossible timeMs
    case _ if moveUntil(character, character.nextTileCenter) > timeMs => character changePosition moveFor(character, timeMs)
    case _ => move(character changePosition character.nextTileCenter, timeMs - moveUntil(character, character.nextTileCenter), desiredDirection)
  }

  implicit private class CharacterEnhanced(character: Character)(implicit map: Map) {
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

  implicit private class MapEnhanced(map: Map) {
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
