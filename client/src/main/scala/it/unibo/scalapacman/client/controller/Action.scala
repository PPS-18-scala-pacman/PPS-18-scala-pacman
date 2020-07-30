package it.unibo.scalapacman.client.controller

sealed trait Action
sealed trait UserIntention extends Action
sealed trait UserAction extends Action

object Action {
  case object START_GAME extends UserIntention
  case object END_GAME extends UserIntention
  case object SUBSCRIBE_TO_GAME_UPDATES extends UserIntention
  case object EXIT_APP extends UserIntention
  case object SAVE_KEY_MAP extends UserIntention
  case object RESET_KEY_MAP extends UserIntention
  case object MOVEMENT extends UserIntention

  case object MOVE_UP extends UserAction
  case object MOVE_DOWN extends UserAction
  case object MOVE_RIGHT extends UserAction
  case object MOVE_LEFT extends UserAction
  case object MOVE_DEFAULT extends UserAction
}
