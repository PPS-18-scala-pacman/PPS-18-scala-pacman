package it.unibo.scalapacman.client.controller

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import it.unibo.scalapacman.client.communication.{ClientHandler, PacmanRestClient}
import it.unibo.scalapacman.client.gui.GUI

import scala.concurrent.ExecutionContextExecutor

object GameMaster {
  def init(): Unit = {
    val pacmanRestClient: PacmanRestClient with ClientHandler = new PacmanRestClient() with ClientHandler {
      val config = ConfigFactory.load()
      override implicit def classicActorSystem: ActorSystem = ActorSystem("ClientActSys", config.getConfig("client-app").withFallback(config))
      override implicit def executionContext: ExecutionContextExecutor = classicActorSystem.dispatcher
    }
    val controller: Controller = Controller(pacmanRestClient)
    GUI(controller)
  }
}
