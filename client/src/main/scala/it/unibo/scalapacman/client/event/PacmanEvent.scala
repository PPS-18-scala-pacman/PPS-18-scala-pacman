package it.unibo.scalapacman.client.event

import it.unibo.scalapacman.client.input.KeyMap
import it.unibo.scalapacman.client.map.PacmanMap.PacmanMap
import it.unibo.scalapacman.lib.model.GameState

trait PacmanEvent

/**
 * Evento aggiornameto di gioco
 * @param map la mappa arricchita dei personaggi
 * @param gameState stato di gioco
 */
case class GameUpdate(map: PacmanMap, gameState: GameState) extends PacmanEvent

/**
 * Evento gioco in pausa / gioco ripreso
 * @param paused rappresenta se il gioco è in pausa o è stato ripreso
 */
case class GamePaused(paused: Boolean) extends PacmanEvent

/**
 * Evento partita iniziata
 */
case class GameStarted() extends PacmanEvent

/**
 * Evento nuova configurazione comandi
 * @param keyMap la nuova configurazione di comandi
 */
case class NewKeyMap(keyMap: KeyMap) extends PacmanEvent

case class NetworkIssue(serverError: Boolean, info: String) extends PacmanEvent
