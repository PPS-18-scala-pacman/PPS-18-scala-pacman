package it.unibo.scalapacman.server.config

import scala.concurrent.duration.FiniteDuration

object TestSettings {
  // scalastyle:off magic.number

  val askTestDuration: FiniteDuration = Settings.askDuration * 2
  val waitTime       : FiniteDuration = Settings.gameRefreshRate * 8

  val awaitLowerBound: FiniteDuration = Settings.gameRefreshRate / 2
  val awaitUpperBound: FiniteDuration = waitTime * 4

  // scalastyle:on magic.number
}
