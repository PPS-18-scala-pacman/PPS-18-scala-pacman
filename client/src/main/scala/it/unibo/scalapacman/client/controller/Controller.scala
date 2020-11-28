package it.unibo.scalapacman.client.controller

import java.util.{Timer, TimerTask}

import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.communication.PacmanRestClient
import Action.{CREATE_LOBBY, END_GAME, EXIT_APP, JOIN_LOBBY, LEAVE_LOBBY, MOVEMENT, PAUSE_RESUME, RESET_KEY_MAP, SAVE_KEY_MAP, START_GAME, SUBSCRIBE_TO_EVENTS}
import it.unibo.scalapacman.client.event.{GamePaused, GameStarted, GameUpdate, LobbiesUpdate, LobbyUpdate, NetworkIssue,
  NewKeyMap, PacmanPublisher, PacmanSubscriber}
import it.unibo.scalapacman.client.gui.{LOBBIES_RECONNECTION_TIME_DELAY, WS_RECONNECTION_TIME_DELAY}
import it.unibo.scalapacman.client.input.JavaKeyBinding.DefaultJavaKeyBinding
import it.unibo.scalapacman.client.input.KeyMap
import it.unibo.scalapacman.client.map.PacmanMap
import it.unibo.scalapacman.client.model.LobbyJsonProtocol.lobbyFormat
import it.unibo.scalapacman.client.model.{CreateLobbyData, GameModel, JoinLobbyData, Lobby}
import it.unibo.scalapacman.common.CommandType.CommandType
import it.unibo.scalapacman.common.MoveCommandType.MoveCommandType
import it.unibo.scalapacman.common.{Command, CommandType, CommandTypeHolder, JSONConverter, MapUpdater, MoveCommandTypeHolder, UpdateModelDTO}
import it.unibo.scalapacman.lib.model.{Map, MapType}
import spray.json.DefaultJsonProtocol.listFormat

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}
import spray.json._ //scalastyle:ignore

// scalastyle:off multiple.string.literals

trait Controller {
  /**
   * Modello che contiene le informazioni principali del gioco
   */
  var model: GameModel

  /**
   * Gestisce le azioni dell'utente
   *
   * @param action tipo di azione avvenuta
   * @param param  parametro che arricchisce l'azione avvenuta con ulteriori informazioni
   */
  def handleAction(action: Action, param: Option[Any]): Unit

  /**
   * Recupera l'ultima azione dell'utente avvenuta in partita
   *
   * @return l'ultima azione dell'utente avvenuta in partita
   */
  def userAction: Option[MoveCommandType]
}

object Controller {
  def apply(pacmanRestClient: PacmanRestClient): Controller = ControllerImpl(pacmanRestClient)
}

/**
 * Implementazione del Controller dell'applicazione
 * @param pacmanRestClient il servizio per la comunicazione col Server
 */
private case class ControllerImpl(pacmanRestClient: PacmanRestClient) extends Controller with Logging {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val UNKNOWN_ACTION = "Azione non riconosciuta"
  /**
   * Configurazione dei tasti iniziale, prende i valori di default di DefaultJavaKeyBinding.
   */
  val _defaultKeyMap: KeyMap = KeyMap(DefaultJavaKeyBinding.UP, DefaultJavaKeyBinding.DOWN, DefaultJavaKeyBinding.RIGHT,
    DefaultJavaKeyBinding.LEFT, DefaultJavaKeyBinding.PAUSE)
  var _prevUserAction: Option[MoveCommandType] = None
  val _publisher: PacmanPublisher = PacmanPublisher()
  val _webSocketRunnable = new WebSocketConsumer(updateFromServer)
  var model: GameModel = GameModel(keyMap = _defaultKeyMap, map = Map.create(MapType.CLASSIC))

  connectToLobbies()

  // scalastyle:off cyclomatic.complexity
  def handleAction(action: Action, param: Option[Any]): Unit = action match {
    case START_GAME => evalStartGame(model.gameId, param.asInstanceOf[Option[CreateLobbyData]])
    case CREATE_LOBBY => evalCreateLobby(model.gameId, model.lobby, param.asInstanceOf[Option[CreateLobbyData]])
    case JOIN_LOBBY => evalJoinLobby(model.gameId, model.lobby, param.asInstanceOf[Option[JoinLobbyData]])
    case LEAVE_LOBBY => evalLeaveLobby(model.gameId, model.lobby)
    case END_GAME => evalEndGame(model.gameId)
    case SUBSCRIBE_TO_EVENTS => evalSubscribeToGameUpdates(param.asInstanceOf[Option[PacmanSubscriber]])
    case MOVEMENT => evalMovement(param.asInstanceOf[Option[MoveCommandType]], _prevUserAction, model.gameId)
    case PAUSE_RESUME => evalPauseResume(param.asInstanceOf[Option[CommandType]], model.paused, model.gameId)
    case SAVE_KEY_MAP => evalSaveKeyMap(param.asInstanceOf[Option[KeyMap]])
    case RESET_KEY_MAP => evalSaveKeyMap(Some(_defaultKeyMap))
    case EXIT_APP => evalExitApp()
    case _ => error(UNKNOWN_ACTION)
  }
  // scalastyle:oon cyclomatic.complexity

  def userAction: Option[MoveCommandType] = _prevUserAction

  /**
   * Se non c'è nessuna partita in corso, procede con la creazione di una nuova partita.
   *
   * @param gameId il valore attuale di gameId ottenuto dal Model
   * @param cgdMaybe oggetto di configurazione per la nuova partita
   */
  private def evalStartGame(gameId: Option[String], cgdMaybe: Option[CreateLobbyData]): Unit = gameId match {
    case None => startGame(cgdMaybe.get)
    case Some(_) => error("Impossibile creare nuova partita quando ce n'è già una in corso")
  }

  /**
   * Se la chiamata ha successo:
   * - re-inizializza il Model
   * - istanzia un nuovo thread per la gestione dei messaggi WebSocket
   * - la chiamata al Server per aprire il canale WebSocket
   * - pubblica l'evento di gioco iniziato tramite il Publisher
   *
   * @param cgd l'oggetto di configurazione per la nuova partita
   */
  private def startGame(cgd: CreateLobbyData): Unit = pacmanRestClient.startGame(cgd.size) onComplete {
    case Success(value) =>
      info(s"Partita creata con successo: id $value") // scalastyle:ignore multiple.string.literals
      model = model.copy(gameId = Some(value)/*, paused = true*/) // Il gioco parte sempre in pausa --> NON PIù
      model = model.copy(username = cgd.username)
      _prevUserAction = None
      new Thread(_webSocketRunnable).start()
      pacmanRestClient.openWS(value, model.username, handleWebSocketMessage, handleWSConnectionError)
      _publisher.notifySubscribers(GameStarted())
    case Failure(exception) =>
      error(s"Errore nella creazione della partita: ${exception.getMessage}")
      _publisher.notifySubscribers(NetworkIssue(serverError = true, "Errore comunicazione col server"))
  }

  /**
   * Se non c'è nessuna partita / lobby in corso, procede con la creazione della lobby
   * @param gameId  il valore attuale di gameId ottenuto dal Model
   * @param lobby il valore attuale di lobby ottenuto dal Model
   * @param cld dati per la creazione della lobby
   */
  private def evalCreateLobby(gameId: Option[String], lobby: Option[Lobby], cld: Option[CreateLobbyData]): Unit = (gameId, lobby) match {
    case (None, None) => createLobby(cld.get)
    case (Some(_), None) => error("Impossibile creare una lobby quando c'è una partita in corso")
    case (None, Some(_)) => error("Impossibile creare una lobby quando ne esiste già una")
    case _ => error("Errore: partita in corso e lobby esistente")
  }

  /**
   * Effettua chiamata creazione lobby
   * @param cld dati per la creazione della lobby
   */
  private def createLobby(cld: CreateLobbyData): Unit = pacmanRestClient.createLobby(cld.username, cld.username, cld.size) onComplete {
    case Success(value) =>
      val lobby: Lobby = value.parseJson.convertTo[Lobby]
      info(s"Lobby ${lobby.description} creata con successo")
      model = model.copy(lobby = Some(lobby))
      connectToLobby(lobby.id)
    case Failure(exception) =>
      error(s"Errore nella creazione della lobby: ${exception.getMessage}")
  }

  /**
   * Se non c'è nessuna partita / lobby in corso, procede con il join alla lobby
   * @param gameId  il valore attuale di gameId ottenuto dal Model
   * @param lobby il valore attuale di lobby ottenuto dal Model
   * @param jld dati per il join della lobby
   */
  private def evalJoinLobby(gameId: Option[String], lobby: Option[Lobby], jld: Option[JoinLobbyData]): Unit = (gameId, lobby) match {
    case (None, None) => pacmanRestClient.joinLobby(jld.get.lobby.id, model.username) onComplete {
      case Success(_) =>
        info(s"Partecipazione a lobby ${jld.get.lobby.description} avvenuta con successo")
        model = model.copy(lobby = Some(jld.get.lobby))
        connectToLobby(jld.get.lobby.id)
      case Failure(exception) =>
        error(s"Errore durante partecipazione lobby ${jld.get.lobby.description}: ${exception.getMessage}")
    }
    case (Some(_), None) => error("Impossibile partecipare ad una lobby quando c'è una partita in corso")
    case (None, Some(_)) => error("Impossibile partecipare ad una lobby quando ne esiste già una")
    case _ => error("Errore: partita in corso e lobby esistente")
  }

  /**
   * Se non c'è una lobby in corso, procede con l'abbandono dalla lobby
   * @param gameId  il valore attuale di gameId ottenuto dal Model
   * @param lobby il valore attuale di lobby ottenuto dal Model
   */

  private def evalLeaveLobby(gameId: Option[String], lobby: Option[Lobby]): Unit = (gameId, lobby) match {
    case (None, Some(lobby)) =>
      pacmanRestClient.leaveLobby(model.username) onComplete {
        case Success(_) =>
          info(s"Lobby ${lobby.description} abbandonata con successo")
          model = model.copy(lobby = None)
        case Failure(exception) =>
          error(s"Errore durante abbandono lobby ${lobby.description}: ${exception.getMessage}")
      }
    case (Some(_), _) => error("Impossibile abbandonare una lobby quando c'è una partita in corso")
    case (None, None) => error("Nessuna lobby da abbandonare")
  }

  /**
   * Termina il gestore dei messaggi WebSocket e chiude il canale WebSocket.
   * Se c'è una partita in corso, effettua la chiamata al Server per terminare la partita, dopodiché pulisce il Model.
   * Le operazioni sul gestore dei messaggi e sulla chiusura del canale WebSocket vengono eseguite ogni volta per
   * garantire che le risorse non restino allocate.
   *
   * @param gameId il
   */
  private def evalEndGame(gameId: Option[String]): Unit = {
    _webSocketRunnable.terminate()
    pacmanRestClient.closeWebSocket()
    gameId match {
      case Some(id) => pacmanRestClient.endGame(id) onComplete {
        case Success(message) =>
          info(s"Partita $id terminata con successo: $message")
          clearModel()
        case Failure(exception) =>
          error(s"Errore nella terminazione della partita: ${exception.getMessage}")
          clearModel()
      }
      case None => info("Nessuna partita da dover terminare")
    }
  }

  /**
   * Effettua la sottoscrizione di un Subscriber al Publisher
   *
   * @param maybeSubscriber il subscriber che vuole sottoscriversi
   */
  private def evalSubscribeToGameUpdates(maybeSubscriber: Option[PacmanSubscriber]): Unit = maybeSubscriber match {
    case None => error("Subscriber mancante, impossibile registrarsi")
    case Some(subscriber) => _publisher.subscribe(subscriber)
  }

  /**
   * Gestisce l'intento dell'utente di muoversi nella mappa.
   * L'informazione sul movimento viene salvata ed inviata al Server solo se c'è una partita in corso.
   *
   * @param newUserAction il nuovo movimento
   * @param prevUserAction il movimento precedente
   * @param gameId identificativo della partita in corso
   */
  private def evalMovement(newUserAction: Option[MoveCommandType], prevUserAction: Option[MoveCommandType], gameId: Option[String]): Unit =
    if (gameId.isDefined) {
      (newUserAction, prevUserAction) match {
        case (Some(newInt), Some(prevInt)) if newInt == prevInt => info("Non invio aggiornamento al server")
        case (None, _) => error("Nuova azione utente è None")
        case _ =>
          info("Invio aggiornamento al server")
          debug(s"Invio al server l'azione ${newUserAction.get} dell'utente")
          _prevUserAction = newUserAction
          sendMovement(newUserAction)
      }
    }

  private def sendMovement(moveCommandType: Option[MoveCommandType]): Unit = sendOverWebsocket(CommandType.MOVE, moveCommandType)

  /**
   * Gestisce l'intento dell'utente di mettere in pausa / riprendere il gioco.
   * L'informazione sullo stato di pausa/ripristino viene salvata ed inviata al Server solo se c'è una partita in corso.
   *
   * @param newPauseResume nuovo stato della partita (Pausa o Ripresa)
   * @param gamePaused stato precedente della partita
   * @param gameId identificativo della partita in corso
   */
  private def evalPauseResume(newPauseResume: Option[CommandType], gamePaused: Boolean, gameId: Option[String]): Unit =
    if (gameId.isDefined) {
      newPauseResume match {
        case Some(CommandType.PAUSE) if gamePaused => info("Gioco già in pausa")
        case Some(CommandType.RESUME) if !gamePaused => info("Gioco già in esecuzione")
        case None => error("Pause/Resume è None")
        case _ =>
          model = model.copy(paused = !model.paused)
          sendPauseResume(newPauseResume.get)
          _publisher.notifySubscribers(GamePaused(model.paused))
      }
    }

  private def sendPauseResume(commandType: CommandType): Unit = sendOverWebsocket(commandType, None)

  /**
   * Invia un messaggio al Server tramite la WebSocket
   *
   * @param commandType tipologia di comando dell'utente (Movimento, Pausa o Ripresa)
   * @param moveCommandType informazioni aggiuntive sul comando (solo se Movimento)
   */
  private def sendOverWebsocket(commandType: CommandType, moveCommandType: Option[MoveCommandType]): Unit = pacmanRestClient.sendOverWebSocket(
    JSONConverter.toJSON(
      Command(
        CommandTypeHolder(commandType),
        moveCommandType.map(moveCommand => JSONConverter.toJSON(MoveCommandTypeHolder(moveCommand)))
      )
    )
  )

  /**
   * Gestisce l'intento dell'utente di salvare una nuova configurazione dei tasti.
   * Pubblica l'evento di nuova configurazione tasti tramite il Publisher
   *
   * @param maybeKeyMap la nuova configurazione di tasti
   */
  private def evalSaveKeyMap(maybeKeyMap: Option[KeyMap]): Unit = maybeKeyMap match {
    case None => error("Configurazione tasti non valida")
    case Some(keyMap) =>
      model = model.copy(keyMap = keyMap)
      _publisher.notifySubscribers(NewKeyMap(model.keyMap))
      info(s"Nuova configurazione dei tasti salvata $keyMap") // scalastyle:ignore multiple.string.literals
  }

  /**
   * Gestisce l'intento dell'utente di uscire dall'applicazione.
   * Esegue le operazioni di termine partita per assicurarsi che non rimangano partite pendenti lato server
   */
  private def evalExitApp(): Unit = {
    evalEndGame(model.gameId)
    info("Chiusura dell'applicazione")
    System.exit(0)
  }

  /**
   * Pulisce il Model, riportandolo alla situazione iniziale (mantenendo invariato però la configurazione dei tasti e il nickname)
   */
  private def clearModel(): Unit = model = model.copy(gameId = None, paused = false, map = Map.create(MapType.CLASSIC))

  /**
   * Aggiorna il Model con le informazioni ricevuta dal Server sulla partita in corso e pubblica l'evento
   * del nuovo aggiornamento tramite il Publisher
   *
   * @param updateModelDTO l'aggiornamento della partita
   */
  private def updateFromServer(updateModelDTO: UpdateModelDTO): Unit = {
    model = model.copy(map = MapUpdater.update(model.map, updateModelDTO.dots, updateModelDTO.fruit))
    _publisher.notifySubscribers(GameUpdate(PacmanMap.createWithCharacters(model.map, updateModelDTO.gameEntities), updateModelDTO.state))
  }

  /**
   * Recupera i messaggi ricevuti dalla WebSocket e li passa al gestore di tali messaggi
   * @param message il messaggio ricevuto sulla WebSocket
   */
  private def handleWebSocketMessage(message: String): Unit = _webSocketRunnable.addMessage(message)

  /**
   * Gestisce il caso in cui la websocket per comunicare col server venga interrotta per un problema di rete
   */
  private def handleWSConnectionError(serverError: Boolean = false): Unit = if (!serverError) {
    if (model.gameId.isDefined) {
      debug(s"[handleWSConnectionError] Connessione websocket chiusa in modo anomalo, tentativo di riconnessione " +
        s"in ${WS_RECONNECTION_TIME_DELAY / 1000} secondi")
      _publisher.notifySubscribers(NetworkIssue(serverError = false, s"Riconnessione al server in corso"))
      val t = new Timer
      t.schedule(new TimerTask() {
        override def run(): Unit = {
          // Devo ricontrollare perché potrebbe essere che nel frattempo l'utente sia uscito dalla schermata di gioco
          if (model.gameId.isDefined) {
            pacmanRestClient.openWS(model.gameId.get, model.username, handleWebSocketMessage, handleWSConnectionError)
          }
          t.cancel()
        }
      }, WS_RECONNECTION_TIME_DELAY)
    }
  } else {
    debug(s"[handleWSConnectionError] Errore nella comunicazione col server, comunicazione tramite WebSocket terminata")
    _publisher.notifySubscribers(NetworkIssue(serverError = true, "Errore comunicazione col server"))
  }

  /**
   * Esegue chiamata connessione a servizio SSE per recupero lista lobby
   */
  private def connectToLobbies(): Unit = pacmanRestClient.watchLobbies(handleLobbiesUpdate, handleLobbiesConnectionError) onComplete {
    case Failure(exception) => info(s"Errore connessione SSE lobbies: ${exception.getMessage}"); handleLobbiesConnectionError()
    case _ => Unit
  }

  /**
   * Gestisce i messaggi ricevuti sul canale SSE delle lobby
   * @param jsonStr i dati in formato JSON
   */
  private def handleLobbiesUpdate(jsonStr: String): Unit =
    _publisher.notifySubscribers(LobbiesUpdate(jsonStr.parseJson.convertTo[List[Lobby]]))

  /**
   * Gestisce l'interruzione al canale SSE delle lobby per un problema di rete
   */
  private def handleLobbiesConnectionError(): Unit = {
    info(s"Nuovo tentativo connessione servizio lobbies tra ${LOBBIES_RECONNECTION_TIME_DELAY/1000} secondi")
    val t = new Timer
    t.schedule(new TimerTask() {
      override def run(): Unit = {
        connectToLobbies()
        t.cancel()
      }
    }, LOBBIES_RECONNECTION_TIME_DELAY)
  }

  /**
   * Esegue chiamata connessione a servizio SSE per recupero informazioni specifica lobby
   * @param lobbyId id della lobby a cui collegarsi
   */
  private def connectToLobby(lobbyId: Int): Unit = pacmanRestClient.watchLobby(lobbyId, handleLobbyUpdate, handleLobbyConnectionError(lobbyId)) onComplete {
    case Failure(exception) => info(s"Errore connessione SSE lobby: ${exception.getMessage}"); handleLobbyConnectionError(lobbyId)()
    case _ => Unit
  }

  /**
   * Gestisce i messaggi ricevuti sul canale SSE della lobby
   * @param jsonStr i dati in formato JSON
   */
  private def handleLobbyUpdate(jsonStr: String): Unit =
    _publisher.notifySubscribers(LobbyUpdate(jsonStr.parseJson.convertTo[Lobby]))

  /**
   * Gestisce l'interruzione al canale SSE delle lobby per un problema di rete
   */
  private def handleLobbyConnectionError(lobbyId: Int)(): Unit = {
    info(s"Nuovo tentativo connessione servizio lobby tra ${LOBBIES_RECONNECTION_TIME_DELAY/1000} secondi")
    val t = new Timer
    t.schedule(new TimerTask() {
      override def run(): Unit = {
        connectToLobby(lobbyId)
        t.cancel()
      }
    }, LOBBIES_RECONNECTION_TIME_DELAY)
  }
}

// scalastyle:on multiple.string.literals
