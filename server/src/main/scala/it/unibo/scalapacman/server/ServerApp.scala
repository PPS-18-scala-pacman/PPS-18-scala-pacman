package it.unibo.scalapacman.server

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.scalapacman.server.communication.HttpService
import it.unibo.scalapacman.server.core.Master
import it.unibo.scalapacman.server.config.ConfLoader.{appConf, akkaConf}

/**
 * Entità per avvio del server
 */
object Bootstrap {
  def apply(): Behavior[String] = Behaviors.setup { context =>

    context.spawn(HttpService(appConf.getString("connection.hostAddr"),
      appConf.getInt("connection.port")),
      "HttpService")

    context.spawn(Master(), "MasterGame")
    Behaviors.same
  }
}

object ServerApp extends App {
  val system = ActorSystem(Bootstrap(), "BootstrapActor", akkaConf)
}
