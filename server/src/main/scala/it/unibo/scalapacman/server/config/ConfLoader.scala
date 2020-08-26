package it.unibo.scalapacman.server.config

import com.typesafe.config.{Config, ConfigFactory}

object ConfLoader {
  val confLoaded: Config = ConfigFactory.load()
  val config: Config = confLoaded.getConfig("server-app").withFallback(confLoaded)
}
