package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.model.{Character, Map}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.engine.CircularMovement.{moveFor, moveUntil}
import it.unibo.scalapacman.lib.engine.GameHelpers.CharacterHelper

import scala.concurrent.duration.FiniteDuration

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
  def move(character: Character, timeMs: Double, desiredDirection: Direction)(implicit map: Map): Character =
    if (timeMs == 0 || character.isDead) {
      character
    } else if (character desireRevert desiredDirection) {
      move(character revert, timeMs, desiredDirection)
    } else if (character.position == character.nextTileCenter) {
      (character changeDirectionIfPossible desiredDirection) moveIfPossible timeMs
    } else if (moveUntil(character, character.nextTileCenter) > timeMs) {
      character.copy(position = moveFor(character, timeMs))
    } else {
      move(character.copy(position = character.nextTileCenter), timeMs - moveUntil(character, character.nextTileCenter), desiredDirection)
    }

  def move(character: Character, time: FiniteDuration, desiredDirection: Option[Direction])(implicit map: Map): Character =
    move(character, time.toMillis, desiredDirection getOrElse character.direction)

}
