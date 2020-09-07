package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.math.{Point2D, TileGeography}
import it.unibo.scalapacman.lib.model.{Character, Direction, Map}

/**
 * Movement with Pacman effect
 */
object CircularMovement {

  /**
   * Sposta un personaggio muovendolo linearmente nella propria direzione per il tempo in input.
   * Applica l'effetto Pacman basandosi sulle dimensioni della mappa.
   * @param character Personaggio da spostare
   * @param timeMs Tempo di spostamento in millisecondi
   * @param map Mappa di gioco
   * @return Il personaggio stesso aggiornato alla nuova posizione.
   */
  def moveFor(character: Character, timeMs: Double)(implicit map: Map): Point2D = map.pacmanEffect(CharacterMovement.moveFor(character, timeMs))

  /**
   * Calcola il tempo di spostamento necessario ad un personaggio per raggiungere un determinato punto.
   * Il tempo viene calcolato muovendo il personaggio linearmente nella propria direzione.
   * Applica l'effetto Pacman basandosi sulle dimensioni della mappa.
   * @param character Personaggio da spostare
   * @param endingPoint Punto di destinazione
   * @param map Mappa di gioco
   * @return Tempo di spostamento in millisecondi
   */
  def moveUntil(character: Character, endingPoint: Point2D)(implicit map: Map): Double = character.direction match {
    case Direction.WEST if character.position.x < endingPoint.x => CharacterMovement.moveUntil(character, Point2D(endingPoint.x - map.width, endingPoint.y))
    case Direction.EAST if character.position.x > endingPoint.x => CharacterMovement.moveUntil(character, Point2D(endingPoint.x + map.width, endingPoint.y))
    case Direction.NORTH if character.position.y < endingPoint.y => CharacterMovement.moveUntil(character, Point2D(endingPoint.x, endingPoint.y - map.height))
    case Direction.SOUTH if character.position.y > endingPoint.y => CharacterMovement.moveUntil(character, Point2D(endingPoint.x, endingPoint.y + map.height))
    case _ => CharacterMovement.moveUntil(character, endingPoint)
  }

  implicit private class MapEnhanced(map: Map) {
    val height: Double = map.tiles.size * TileGeography.SIZE
    val width: Double = map.tiles.head.size * TileGeography.SIZE

    def pacmanEffect(point2D: Point2D): Point2D = Point2D(pacmanEffect(point2D.x, width), pacmanEffect(point2D.y, height))

    @scala.annotation.tailrec
    private def pacmanEffect(x: Double, max: Double): Double = x match {
      case x: Double if x >= 0 => x % max
      case x: Double => pacmanEffect(x + max, max)
    }
  }

}
