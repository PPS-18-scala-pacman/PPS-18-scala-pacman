package it.unibo.scalapacman.client.controller

import akka.actor.ActorSystem
import it.unibo.scalapacman.client.communication.{ClientHandler, PacmanRestClient}
import it.unibo.scalapacman.client.gui.GUI

import scala.concurrent.ExecutionContextExecutor

object GameMaster {
  def init(): Unit = {
    val pacmanRestClient: PacmanRestClient with ClientHandler = new PacmanRestClient() with ClientHandler {
      override implicit def classicActorSystem: ActorSystem = ActorSystem()
      override implicit def executionContext: ExecutionContextExecutor = classicActorSystem.dispatcher
    }
    val controller: Controller = Controller(pacmanRestClient)
    GUI(controller)
  }
}
