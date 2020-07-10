package it.unibo.scalapacman.client.communication

sealed trait Message
sealed trait MovementMessage

object Message {
  case object RIGHT_PRESSED extends MovementMessage
  case object RIGHT_RELEASED extends MovementMessage
  case object LEFT_PRESSED extends MovementMessage
  case object LEFT_RELEASED extends MovementMessage
  case object UP_PRESSED extends MovementMessage
  case object UP_RELEASED extends MovementMessage
  case object DOWN_PRESSED extends MovementMessage
  case object DOWN_RELEASED extends MovementMessage
}
