package it.unibo.scalapacman.client.utils

import grizzled.slf4j.Logger
import it.unibo.scalapacman.client.utils.UserDialog.{showError, showWarning}

trait PacmanLogger {
  def info(msg: String): Unit
  def error(msg: String): Unit
  def warning(msg: String): Unit
  def debug(msg: String): Unit
}

object PacmanLogger {

  def apply(): PacmanLogger = new PacmanLoggerImpl()

  private class PacmanLoggerImpl() extends PacmanLogger {

    private val logger = Logger(classOf[PacmanLoggerImpl])

    override def info(msg: String): Unit = logger.info(msg)

    override def error(msg: String): Unit = {
      logger.error(msg)
      showError(msg)
    }

    override def warning(msg: String): Unit = {
      logger.warn(msg)
      showWarning(msg)
    }

    override def debug(msg: String): Unit = logger.debug(msg)
  }
}
