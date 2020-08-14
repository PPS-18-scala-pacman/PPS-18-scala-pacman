package it.unibo.scalapacman.server.util

import scala.concurrent.duration.FiniteDuration

object TestSettings {

  val askTestDuration: FiniteDuration = Settings.askDuration * 2
  val waitTime       : FiniteDuration = Settings.gameRefreshRate * 8
}
