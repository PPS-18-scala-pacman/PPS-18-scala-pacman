package it.unibo.scalapacman.common

object CommandType extends Enumeration {
  type CommandType = Value
  val MOVE, PAUSE, RESUME, LEFT_GAME = Value
}

object MoveCommandType extends Enumeration {
  type MoveCommandType = Value
  val UP, DOWN, RIGHT, LEFT, NONE = Value
}

/**
 * Classe rappresentante l'azione dell'utente che il Client invia al Server
 *
 * @param id    la tipologia di azione
 * @param data  ulteriori informazioni sull'azione
 */
case class Command(id: CommandTypeHolder, data: Option[String])
