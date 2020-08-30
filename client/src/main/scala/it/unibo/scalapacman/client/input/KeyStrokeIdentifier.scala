package it.unibo.scalapacman.client.input

sealed trait KeyStrokeIdentifier

object KeyStrokeIdentifier {
  case object UP_PRESSED extends KeyStrokeIdentifier
  case object UP_RELEASED extends KeyStrokeIdentifier
  case object DOWN_PRESSED extends KeyStrokeIdentifier
  case object DOWN_RELEASED extends KeyStrokeIdentifier
  case object RIGHT_PRESSED extends KeyStrokeIdentifier
  case object RIGHT_RELEASED extends KeyStrokeIdentifier
  case object LEFT_PRESSED extends KeyStrokeIdentifier
  case object LEFT_RELEASED extends KeyStrokeIdentifier
  case object PAUSE_RESUME extends KeyStrokeIdentifier
}
