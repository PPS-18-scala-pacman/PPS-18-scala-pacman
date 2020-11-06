package it.unibo.scalapacman.client.controller

sealed trait Action

/**
 * Azioni che rappresentano la volontà dell'utente
 */
object Action {
  case object START_GAME extends Action
  case object START_GAME_MULTI extends Action
  case object JOIN_GAME_MULTI extends Action
  case object END_GAME extends Action
  case object SUBSCRIBE_TO_EVENTS extends Action
  case object EXIT_APP extends Action
  case object SAVE_KEY_MAP extends Action
  case object RESET_KEY_MAP extends Action
  case object MOVEMENT extends Action
  case object PAUSE_RESUME extends Action
}
