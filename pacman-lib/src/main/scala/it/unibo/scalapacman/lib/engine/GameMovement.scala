package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.model.{Character, Direction, Map}
import it.unibo.scalapacman.lib.engine.CircularMovement.{moveFor, moveUntil}
import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper}

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

}
