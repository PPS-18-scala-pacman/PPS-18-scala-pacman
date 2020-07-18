package it.unibo.scalapacman.server.util

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object TestSettings {

  val askTestDuration: FiniteDuration = Settings.askDuration * 2
}
