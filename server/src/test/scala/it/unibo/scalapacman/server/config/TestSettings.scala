package it.unibo.scalapacman.server.config

import scala.concurrent.duration.FiniteDuration

object TestSettings {

  val askTestDuration: FiniteDuration = Settings.askDuration * 2
  val waitTime       : FiniteDuration = Settings.gameRefreshRate * 8
}
