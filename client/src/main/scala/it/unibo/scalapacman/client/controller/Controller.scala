package it.unibo.scalapacman.client.controller

import akka.actor.ActorSystem
import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.communication.{ClientHandler, PacmanRestClient}
import it.unibo.scalapacman.client.controller.Controller.Actions.Actions

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}

trait Controller {
  def handleAction(action: Actions): Unit
}

object Controller extends Logging {
  object Actions extends Enumeration {
    type Actions = Value

    val START_GAME, END_GAME, EXIT_APP = Value
  }

  // Necessari per gestire la `onComplete`
  implicit def classicActorSystem: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = classicActorSystem.dispatcher

  private val UNKNOWN_ACTION = "Azione non riconosciuta"

  private var gameId: Option[String] = None

  private val pacmanRestClient = new PacmanRestClient() with ClientHandler {
    override implicit def classicActorSystem: ActorSystem = ActorSystem()
    override implicit def executionContext: ExecutionContextExecutor = classicActorSystem.dispatcher
  }

  def handleAction(action: Actions): Unit = action match {
    case Actions.START_GAME => doStartGame()
    case Actions.EXIT_APP => doExitApp()
    case _ => error(UNKNOWN_ACTION)
  }

  def test(text: String): Unit = debug(text)

  private def doStartGame(): Unit = {
    pacmanRestClient.startGame onComplete {
      case Success(value) => gameId = Some(value); info(s"Partita creata con successo: id $value") // scalastyle:ignore multiple.string.literals
      case Failure(exception) => error(s"Errore nella creazione della partita: ${exception.getMessage}")
    }
  }

  private def doEndGame(gameId: String): Unit = {
    pacmanRestClient.endGame(gameId) onComplete {
      case Success(message) => this.gameId = None; info(s"Partita terminata con successo: $message")
      case Failure(exception) => this.gameId = None; error(s"Errore nella terminazione della partita: ${exception.getMessage}")
    }
  }

  private def doExitApp(): Unit = gameId match {
    case Some(value) => doEndGame(value); System.exit(0)
    case None => System.exit(0)
  }
}
