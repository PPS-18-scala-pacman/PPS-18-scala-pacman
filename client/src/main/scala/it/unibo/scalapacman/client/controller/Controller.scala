package it.unibo.scalapacman.client.controller

import akka.actor.ActorSystem
import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.communication.{ClientHandler, PacmanRestClient}
import it.unibo.scalapacman.client.utility.Action.{CHANGE_VIEW, END_GAME, EXIT_APP, KEY_PRESSED, KEY_RELEASED, MOVEMENT, SAVE_CONFIGURATION, START_GAME}
import it.unibo.scalapacman.client.gui.GUI
import it.unibo.scalapacman.client.utility.{Action, KeyTap, View}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}

trait Controller {
  def handleAction(action: Action, params: Option[Any]): Unit
}

object GameController extends Controller with Logging {

  // Necessari per gestire la `onComplete`
  implicit def classicActorSystem: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = classicActorSystem.dispatcher

  private val UNKNOWN_ACTION = "Azione non riconosciuta"

  private var gameId: Option[String] = None

  case class KeyState(var key: Option[String] = None, var keyTap: Option[KeyTap] = None)

  private var keyState = KeyState()

  private val pacmanRestClient = new PacmanRestClient() with ClientHandler {
    override implicit def classicActorSystem: ActorSystem = ActorSystem()
    override implicit def executionContext: ExecutionContextExecutor = classicActorSystem.dispatcher
  }

  def handleAction(action: Action, params: Option[Any]): Unit = action match {
    case START_GAME => doStartGame()
    case END_GAME => doEndGame()
    case CHANGE_VIEW => doChangeView(params.asInstanceOf[Option[View]])
    case KEY_PRESSED | KEY_RELEASED => doSendMovement(action.asInstanceOf[KeyTap], params.asInstanceOf[Option[String]])
    case SAVE_CONFIGURATION => doSaveConfiguration()
    case EXIT_APP => doExitApp()
    case _ => error(UNKNOWN_ACTION)
  }

  private def doStartGame(): Unit = gameId match {
    case None => pacmanRestClient.startGame onComplete {
      case Success(value) => gameId = Some(value); info(s"Partita creata con successo: id $value") // scalastyle:ignore multiple.string.literals
      case Failure(exception) => error(s"Errore nella creazione della partita: ${exception.getMessage}")
    }
    case Some(_) => error("Impossibile creare nuova partita quando ce n'è già una in corso")
  }

  private def doEndGame(): Unit = gameId match {
    case Some(id) => pacmanRestClient.endGame(id) onComplete {
      case Success(message) => this.gameId = None; info(s"Partita terminata con successo: $message")
      case Failure(exception) => this.gameId = None; error(s"Errore nella terminazione della partita: ${exception.getMessage}")
    }
    case None => info("Nessuna partita da dover terminare")
  }

  private def doChangeView(view: Option[View]): Unit = view match {
    case Some(view) => GUI.changeView(view.name)
    case None => error("Nessuna view scelta, impossibile cambiare finestra")
  }

  private def doSendMovement(keyTap: KeyTap, key: Option[String]): Unit = keyState match {
    case x: KeyState if x.key.contains(key.get) & x.keyTap.contains(keyTap) => info("Non invio aggiornamento al server")
    case _ =>
      info("Invio aggiornamento al server")
      keyState = KeyState(key, Some(keyTap))
      sendMovement(keyState.keyTap.get, keyState.key.get)
  }

  private def sendMovement(keyTap: KeyTap, key: String): Unit = debug(s"Invio al server il tasto $key che è stato ${keyTap.toString}")

  private def doSaveConfiguration(): Unit = debug("TODO doSaveConfiguration")

  private def doExitApp(): Unit = {
    doEndGame()
    info("Chiusura dell'applicazione")
    System.exit(0)
  }
}
