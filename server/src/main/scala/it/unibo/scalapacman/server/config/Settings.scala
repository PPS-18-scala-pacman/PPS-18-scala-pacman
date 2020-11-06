package it.unibo.scalapacman.server.config

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.DurationInt
import akka.util.Timeout
import it.unibo.scalapacman.server.config.ConfLoader.appConf

import scala.concurrent.duration.FiniteDuration

object Settings {
  // scalastyle:off magic.number

  val askDuration: FiniteDuration = 3.seconds
  implicit val askTimeout: Timeout = askDuration

  val gameRefreshRate : FiniteDuration = FiniteDuration(appConf.getInt("settings.refresh-rate.game"),  TimeUnit.MILLISECONDS)
  val pauseRefreshRate: FiniteDuration = FiniteDuration(appConf.getInt("settings.refresh-rate.pause"), TimeUnit.MILLISECONDS)

  val stashSize: Int = appConf.getInt("settings.stash-size")

  val bufferSizeWS: Int = appConf.getInt("connection.buffer-size")

  val levelDifficulty = 1

  val maxPlayersNumber = 4

  // scalastyle:on magic.number
}
