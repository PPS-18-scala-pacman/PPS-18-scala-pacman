package it.unibo.scalapacman.server.util

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration

object Settings {

  val timeout = FiniteDuration(1, TimeUnit.SECONDS)

}
