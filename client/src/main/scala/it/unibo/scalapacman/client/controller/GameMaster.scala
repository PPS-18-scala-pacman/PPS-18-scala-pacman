package it.unibo.scalapacman.client.controller

import akka.actor.ActorSystem
import it.unibo.scalapacman.client.communication.{ClientHandler, PacmanRestClient}
import it.unibo.scalapacman.client.config.ConfLoader
import it.unibo.scalapacman.client.gui.GUI
import it.unibo.scalapacman.client.utils.PacmanLogger

import javax.swing.UIManager
import scala.concurrent.ExecutionContextExecutor

/**
 * Effettua il bootstrapping del client, istanziando il gestore della comunicazione col server, il controller
 * e il gestore dell'interfaccia grafica
 */
object GameMaster {
  def init(): Unit = {
    val logger = PacmanLogger()
    UIManager.put("OptionPane.yesButtonText", "Sì")

    val pacmanRestClient: PacmanRestClient with ClientHandler = new PacmanRestClient() with ClientHandler {
      override implicit def classicActorSystem: ActorSystem = ActorSystem("ClientActSys", ConfLoader.akkaConf)
      override implicit def executionContext: ExecutionContextExecutor = classicActorSystem.dispatcher
    }
    val controller: Controller = Controller(pacmanRestClient, logger)
    GUI(controller)
  }
}
