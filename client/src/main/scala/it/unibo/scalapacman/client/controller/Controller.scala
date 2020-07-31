package it.unibo.scalapacman.client.controller

import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.communication.PacmanRestClient
import Action.{END_GAME, EXIT_APP, MOVEMENT, RESET_KEY_MAP, SAVE_KEY_MAP, START_GAME, SUBSCRIBE_TO_GAME_UPDATES}
import it.unibo.scalapacman.client.event.{GameUpdate, PacmanPublisher, PacmanSubscriber}
import it.unibo.scalapacman.client.input.JavaKeyBinding.DefaultJavaKeyBinding
import it.unibo.scalapacman.client.input.KeyMap
import it.unibo.scalapacman.client.map.MapBuilder
import it.unibo.scalapacman.common.MoveCommandType.MoveCommandType
import it.unibo.scalapacman.common.{Command, CommandType, CommandTypeHolder, MoveCommandTypeHolder}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
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

  /**
   * Recupera l'ultima azione dell'utente avvenuta in partita
   * @return  l'ultima azione dell'utente avvenuta in partita
   */
  def getUserAction: Option[MoveCommandType]
}

object Controller {
  def apply(pacmanRestClient: PacmanRestClient): Controller = ControllerImpl(pacmanRestClient)
}

private case class ControllerImpl(pacmanRestClient: PacmanRestClient) extends Controller with Logging {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val UNKNOWN_ACTION = "Azione non riconosciuta"
  /**
   * Mappatura dei tasti iniziale, prende i valori di default di DefaultJavaKeyBinding.
   * Viene utilizzata in PlayView per applicare la mappatura iniziale della board di gioco.
   * Viene utilizzata in OptionsView per inizializzare i campi di testo.
   */
  val _defaultKeyMap: KeyMap = KeyMap(DefaultJavaKeyBinding.UP, DefaultJavaKeyBinding.DOWN, DefaultJavaKeyBinding.RIGHT, DefaultJavaKeyBinding.LEFT)
  var _keyMap: KeyMap = _defaultKeyMap
  var _gameId: Option[String] = None
  var _prevUserAction: Option[MoveCommandType] = None
  val _publisher: PacmanPublisher = PacmanPublisher()

  def handleAction(action: Action, param: Option[Any]): Unit = action match {
    case START_GAME => evalStartGame()
    case END_GAME => evalEndGame()
    case SUBSCRIBE_TO_GAME_UPDATES => evalSubscribeToGameUpdates(param.asInstanceOf[Option[PacmanSubscriber]])
    case MOVEMENT => evalMovement(param.asInstanceOf[Option[MoveCommandType]])
    case SAVE_KEY_MAP => evalSaveKeyMap(param.asInstanceOf[Option[KeyMap]])
    case RESET_KEY_MAP => evalSaveKeyMap(Some(_defaultKeyMap))
    case EXIT_APP => evalExitApp()
    case _ => error(UNKNOWN_ACTION)
  }

  def getKeyMap: KeyMap = _keyMap
  def getUserAction: Option[MoveCommandType] = _prevUserAction

  private def evalStartGame(): Unit = _gameId match {
    case None => pacmanRestClient.startGame onComplete {
      case Success(value) =>
        _prevUserAction = None
        info(s"Partita creata con successo: id $value") // scalastyle:ignore multiple.string.literals
        _gameId = Some(value)
        pacmanRestClient.openWS(value, handleWebSocketMessage)
      case Failure(exception) => error(s"Errore nella creazione della partita: ${exception.getMessage}") // scalastyle:ignore multiple.string.literals
    }
    case Some(_) => error("Impossibile creare nuova partita quando ce n'è già una in corso")
  }

  private def evalEndGame(): Unit = _gameId match {
    case Some(id) => pacmanRestClient.endGame(id) onComplete  {
      case Success(message) =>
        info(s"Partita $id terminata con successo: $message")
        _gameId = None
      case Failure(exception) =>
        error(s"Errore nella terminazione della partita: ${exception.getMessage}")
        _gameId = None
    }
    case None => info("Nessuna partita da dover terminare")
  }

  private def evalSubscribeToGameUpdates(maybeSubscriber: Option[PacmanSubscriber]): Unit = maybeSubscriber match {
    case None => error("Subscriber mancante, impossibile registrarsi")
    case Some(subscriber) => _publisher.subscribe(subscriber)
  }

  private def handleWebSocketMessage(message: String): Unit = {
    debug(s"Ricevuto messaggio dal server: $message")
    // TODO fare conversione da JSON ad oggetto
    _publisher.notifySubscribers(GameUpdate(MapBuilder.buildClassic()))
  }

  private def evalMovement(newUserAction: Option[MoveCommandType]): Unit = (newUserAction, _prevUserAction) match {
    case (Some(newInt), Some(prevInt)) if newInt == prevInt => info("Non invio aggiornamento al server")
    case (None, _) => error("Nuova azione utente è None")
    case _ =>
      info("Invio aggiornamento al server")
      debug(s"Invio al server l'azione ${newUserAction.get} dell'utente")
      _prevUserAction = newUserAction
      sendMovement(newUserAction.get)
  }

  private def sendMovement(moveCommandType: MoveCommandType): Unit = _gameId match {
    case None => info("Nessuna partita in corso, non invio informazione movimento al server")
    case _ => pacmanRestClient.sendOverWebSocket(
      ConversionUtils.convertCommand(
        Command(
          CommandTypeHolder(CommandType.MOVE),
          Some(ConversionUtils.convertMoveCommandTypeHolder(MoveCommandTypeHolder(moveCommandType)))
        )
      )
    )
  }

  private def evalSaveKeyMap(maybeKeyMap: Option[KeyMap]): Unit = maybeKeyMap match {
    case None => error("Configurazione tasti non valida")
    case Some(keyMap) =>
      _keyMap = keyMap
      info(s"Nuova configurazione dei tasti salvata $keyMap") // scalastyle:ignore multiple.string.literals
  }

  private def evalExitApp(): Unit = {
    evalEndGame()
    info("Chiusura dell'applicazione")
    System.exit(0)
  }
}
