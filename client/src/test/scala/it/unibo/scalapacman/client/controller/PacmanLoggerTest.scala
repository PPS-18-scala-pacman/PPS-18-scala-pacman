package it.unibo.scalapacman.client.controller

import it.unibo.scalapacman.client.utils.PacmanLogger

class PacmanLoggerTest extends PacmanLogger {
  override def info(msg: String): Unit = Unit

  override def error(msg: String): Unit = Unit

  override def warning(msg: String): Unit = Unit

  override def debug(msg: String): Unit = Unit
}
