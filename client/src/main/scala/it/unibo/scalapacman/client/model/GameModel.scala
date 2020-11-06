package it.unibo.scalapacman.client.model

import it.unibo.scalapacman.client.input.KeyMap
import it.unibo.scalapacman.lib.model.Map

/**
 * Rappresenta le informazioni più importanti di una partita corrente
 * @param gameId id della partita in corso
 * @param paused a true se il gioco è in pausa
 * @param keyMap attuale configurazione dei tasti
 * @param map lo stato attuale della mappa di gioco (senza le informazioni dei personaggi)
 */
case class GameModel(
                      nickname: String = "GioL",
                      gameId: Option[String] = None,
                      paused: Boolean = false,
                      keyMap: KeyMap,
                      map: Map,
                    )
