package it.unibo.scalapacman.client.controller

import akka.actor.ActorSystem
import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.communication.{ClientHandler, PacmanRestClient}
import Action.{CHANGE_VIEW, END_GAME, EXIT_APP, MOVEMENT, START_GAME}
import it.unibo.scalapacman.client.gui.{GUI, View}
import it.unibo.scalapacman.client.input.JavaKeyBinding.DefaultJavaKeyBinding
import it.unibo.scalapacman.client.input.KeyMap

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

trait Controller {
  /**
   * Gestisce le azioni dell'utente
   * @param action  tipo di azione avvenuta
   * @param param  parametro che arricchisce l'azione avvenuta con ulteriori informazioni
   */
  def handleAction(action: Action, param: Option[Any]): Unit

  /**
   * Recupera la mappatura iniziale
   * @return  la mappatura iniziale
   */
  def getKeyMap: KeyMap
}

object Controller {
  def apply(): Controller = ControllerImpl()
}

private case class ControllerImpl() extends Controller with Logging {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  private val UNKNOWN_ACTION = "Azione non riconosciuta"
  /**
   * Mappatura dei tasti iniziale, prende i valori di default di DefaultJavaKeyBinding.
   * Viene utilizzata in PlayView per applicare la mappatura iniziale della board di gioco.
   * Viene utilizzata in OptionsView per inizializzare i campi di testo.
   */
  private val _keyMap: KeyMap = KeyMap(DefaultJavaKeyBinding.UP, DefaultJavaKeyBinding.DOWN, DefaultJavaKeyBinding.RIGHT, DefaultJavaKeyBinding.LEFT)
  private var _gameId: Option[String] = None
  private var _prevUserAction: Option[UserAction] = None

  def handleAction(action: Action, param: Option[Any]): Unit = action match {
    case START_GAME => evalStartGame()
    case END_GAME => evalEndGame()
    case CHANGE_VIEW => evalChangeView(param.asInstanceOf[Option[View]])
    case MOVEMENT => evalSendMovement(param.asInstanceOf[Option[UserAction]])
    case EXIT_APP => evalExitApp()
    case _ => error(UNKNOWN_ACTION)
  }

  def getKeyMap: KeyMap = _keyMap

  private def evalStartGame(): Unit = _gameId match {
    case None => Actions.doStartGame() onComplete {
      case Success(value) =>
        _prevUserAction = None
        info(s"Partita creata con successo: id $value") // scalastyle:ignore multiple.string.literals
        Some(value)
      case Failure(exception) => error(s"Errore nella creazione della partita: ${exception.getMessage}")
    }
    case Some(_) => error("Impossibile creare nuova partita quando ce n'è già una in corso")
  }

  private def evalEndGame(): Unit = _gameId match {
    case Some(id) => Actions.doEndGame(id) onComplete  {
      case Success(message) =>
        info(s"Partita terminata con successo: $message")
        _gameId = None
      case Failure(exception) =>
        error(s"Errore nella terminazione della partita: ${exception.getMessage}")
        _gameId = None
    }
    case None => info("Nessuna partita da dover terminare")
  }

  private def evalChangeView(view: Option[View]): Unit = view match {
    case Some(view) => Actions.doChangeView(view.name)
    case None => error("Nessuna view scelta, impossibile cambiare finestra")
  }

  private def evalSendMovement(newUserAction: Option[UserAction]): Unit = (newUserAction, _prevUserAction) match {
    case (Some(newInt), Some(prevInt)) if newInt == prevInt => info("Non invio aggiornamento al server")
    case (None, _) => error("Nuova azione utente è None")
    case _ =>
      info("Invio aggiornamento al server")
      Actions.doSendMovement(newUserAction.get)
      _prevUserAction = newUserAction
  }

  private def evalExitApp(): Unit = {
    _gameId match {
      case Some(id) =>
        Actions.doEndGame(id)
      case None => // do nothing
    }
    Actions.doExitApp()
  }
}

object Actions extends Logging {
  private val pacmanRestClient = new PacmanRestClient() with ClientHandler {
    override implicit def classicActorSystem: ActorSystem = ActorSystem()
    override implicit def executionContext: ExecutionContextExecutor = classicActorSystem.dispatcher
  }

  def doStartGame(): Future[String] = pacmanRestClient.startGame

  def doEndGame(id: String): Future[String] = pacmanRestClient.endGame(id)

  def doChangeView(viewName: String): Unit = GUI.changeView(viewName)

  def doSendMovement(userAction: UserAction): Unit = debug(s"Invio al server l'azione $userAction dell'utente")

  def doExitApp(): Unit = {
    info("Chiusura dell'applicazione")
    System.exit(0)
  }
}
