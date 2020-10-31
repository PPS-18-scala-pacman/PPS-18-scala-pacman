package it.unibo.scalapacman.server.config

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationInt

import akka.util.Timeout

import scala.concurrent.duration.FiniteDuration

object Settings {
  // scalastyle:off magic.number

  val askDuration: FiniteDuration = 3.seconds
  implicit val askTimeout: Timeout = askDuration

  val gameRefreshRate: FiniteDuration = FiniteDuration(64, TimeUnit.MILLISECONDS)
  val pauseRefreshRate: FiniteDuration = FiniteDuration(500, TimeUnit.MILLISECONDS)

  val stashSize = 100

  val bufferSizeWS = 100

  val levelDifficulty = 1

  // scalastyle:on magic.number
}
