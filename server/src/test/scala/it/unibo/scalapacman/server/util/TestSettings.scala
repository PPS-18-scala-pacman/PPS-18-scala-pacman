package it.unibo.scalapacman.server.util

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object TestSettings {

  val askTestDuration: FiniteDuration = Settings.askDuration * 2
  val waitTime: FiniteDuration = FiniteDuration(10, TimeUnit.SECONDS)// scalastyle:ignore magic.number
}
