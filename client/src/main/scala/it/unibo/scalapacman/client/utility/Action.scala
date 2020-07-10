package it.unibo.scalapacman.client.utility

sealed trait Action
sealed trait UserIntention extends Action
sealed trait KeyTap extends Action

object Action {
  case object START_GAME extends UserIntention
  case object END_GAME extends UserIntention
  case object EXIT_APP extends UserIntention
  case object CHANGE_VIEW extends UserIntention
  case object MOVEMENT extends UserIntention
  case object SAVE_CONFIGURATION extends UserIntention

  case object KEY_PRESSED extends KeyTap
  case object KEY_RELEASED extends KeyTap
}
