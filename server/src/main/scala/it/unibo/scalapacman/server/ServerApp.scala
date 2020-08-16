package it.unibo.scalapacman.server

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory
import it.unibo.scalapacman.server.communication.HttpService
import it.unibo.scalapacman.server.core.Master
import it.unibo.scalapacman.server.util.Settings

object Bootstrap {
  def apply(): Behavior[String] = Behaviors.setup { context =>
    context.spawn(HttpService(Settings.hostAddr, Settings.port), "HttpService")
    context.spawn(Master(), "MasterGame")
    Behaviors.same
  }
}

object ServerApp extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem(Bootstrap(), "BootstrapActor", config.getConfig("server-app").withFallback(config))
}
