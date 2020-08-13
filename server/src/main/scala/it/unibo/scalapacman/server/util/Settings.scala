package it.unibo.scalapacman.server.util

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationInt

import akka.util.Timeout

import scala.concurrent.duration.FiniteDuration

object Settings {
  // scalastyle:off magic.number

  val askDuration: FiniteDuration = 3.seconds
  implicit val askTimeout: Timeout = askDuration

  val hostAddr = "localhost"
  val port = 8080

  val gameRefreshRate: FiniteDuration = FiniteDuration(32, TimeUnit.MILLISECONDS)

  val stashSize = 100

  val bufferSizeWS = 100

  // scalastyle:on magic.number
}
