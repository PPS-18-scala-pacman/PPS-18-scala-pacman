package it.unibo.scalapacman.client.event

import it.unibo.scalapacman.client.input.KeyMap
import it.unibo.scalapacman.client.map.PacmanMap.PacmanMap
import it.unibo.scalapacman.client.model.Lobby
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

/**
 * Evento errore di rete
 * @param serverError se l'errore è dato dal server
 * @param info  informazioni aggiuntive
 */
case class NetworkIssue(serverError: Boolean, info: String) extends PacmanEvent

/**
 * Evento nuove lobby
 * @param lobbies la lista di nuove lobby
 */
case class LobbiesUpdate(lobbies: List[Lobby]) extends PacmanEvent

/**
 * Evento aggiornamento singola lobby
 * @param lobby - le nuove informazioni della lobby
 */
case class LobbyUpdate(lobby: Lobby) extends PacmanEvent
