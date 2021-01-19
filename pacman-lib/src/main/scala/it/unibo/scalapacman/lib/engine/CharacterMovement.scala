package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.math.{Motion, Point2D, TileGeography, Vector2D}
import it.unibo.scalapacman.lib.model.Direction.{EAST, NORTH, NORTHEAST, NORTHWEST, SOUTH, SOUTHEAST, SOUTHWEST, WEST}
import it.unibo.scalapacman.lib.model.Character
import it.unibo.scalapacman.lib.model.Direction.Direction

object CharacterMovement {
  /**
   * Sposta un personaggio muovendolo linearmente nella propria direzione per il tempo in input.
   * @param character Personaggio da spostare
   * @param timeMs Tempo di spostamento in millisecondi
   * @return Il personaggio stesso aggiornato alla nuova posizione.
   */
  def moveFor(character: Character, timeMs: Double): Point2D =
    Motion.uniformLinearFor(character.position, character.speedVector, timeMs)

  /**
   * Calcola il tempo di spostamento necessario ad un personaggio per raggiungere un determinato punto.
   * Il tempo viene calcolato muovendo il personaggio linearmente nella propria direzione.
   * @param character Personaggio da spostare
   * @param endingPoint Punto di destinazione
   * @return Tempo di spostamento in millisecondi
   */
  def moveUntil(character: Character, endingPoint: Point2D): Double =
    Motion.uniformLinearUntil(character.position, endingPoint, character.speedVector)

  implicit private class CharacterEnhanced(character: Character) {
    def speedVector: Vector2D = unitVector(character.direction) * character.speed
  }

  /**
   * Associa un versore ad ogni direzione di tipo Direction
   * @param direction La direzione
   * @return Il versore associato
   */
  def unitVector(direction: Direction): Vector2D = direction match {
    case NORTH => Vector2D(0, -1)
    case SOUTH => Vector2D(0, 1)
    case WEST => Vector2D(-1, 0)
    case EAST => Vector2D(1, 0)
    case NORTHWEST => unitVector(NORTH) + unitVector(WEST)
    case NORTHEAST => unitVector(NORTH) + unitVector(EAST)
    case SOUTHWEST => unitVector(SOUTH) + unitVector(WEST)
    case SOUTHEAST => unitVector(SOUTH) + unitVector(EAST)
  }

  /**
   * Associa un vettore ad ogni direzione, il cui verso dipende dalla direzione
   * mentre l'intensità è pari alla dimensione di TileGeography.SIZE
   * @param direction
   * @return
   */
  def vector(direction: Direction): Vector2D = unitVector(direction) * TileGeography.SIZE
}
