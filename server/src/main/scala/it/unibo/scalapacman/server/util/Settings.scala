package it.unibo.scalapacman.server.util

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationInt

import akka.util.Timeout

import scala.concurrent.duration.FiniteDuration

object Settings {

  implicit val askTimeout: Timeout = 3.seconds

  val hostAddr = "localhost"
  val port = 8080

  val gameRefreshRate: FiniteDuration = FiniteDuration(1, TimeUnit.SECONDS)

  val stashSize = 100
}
