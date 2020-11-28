package it.unibo.scalapacman.client.communication

import java.io.IOException

import akka.{Done, NotUsed}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.client.RequestBuilding.{Delete, Get, Post}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Sink, Source}
import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.config.ConfLoader.appConf
import spray.json.{JsNumber, JsObject, JsString}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling._ // scalastyle:ignore
import it.unibo.scalapacman.client.model.LobbySSEEventType // scalastyle:ignore

// scalastyle:off multiple.string.literals

/**
 * Interfaccia per la gestione della comunicazione con il server
 * Implementa le chiamate per inizio/fine partita
 * e gestione canale di comunicazione per aggiornamenti di gioco
 */
trait PacmanRestClient extends Logging { this: HttpClient =>
  implicit def classicActorSystem: ActorSystem
  implicit def executionContext: ExecutionContextExecutor

  val WS_BUFFER_SIZE: Int = 1
  val TEXT_MESSAGE_TO_STRICT: Int = 100

  var _webSocketSpeaker: Option[ActorRef] = None

  /**
   * Si connette al canale SSE per il recupero della lista di lobby attualmente attive
   * e di loro aggiornamenti
   * @param messageHandler  callback a cui vengono passati i dati delle lobby
   * @param connectionErrorHandler  callback per errore di connessione al servizio
   */
  def watchLobbies(
                    messageHandler: String => Unit,
                    connectionErrorHandler: () => Unit,
                  ): Future[Any] =
    connectSSE(
      PacmanRestClient.LOBBY_URL,
      messageHandler,
      connectionErrorHandler,
      _ => true
    )

  /**
   * Si connette al canale SSE per il recupero della lobby e dei suoi aggiornamenti
   * @param id  identificativo della lobby
   * @param messageHandler  callback a cui vengono passati i dati della lobby
   * @param connectionErrorHandler  callback per errore di connessione al servizio
   */
  def watchLobby(
                  id: Int,
                  messageHandler: String => Unit,
                  connectionErrorHandler: () => Unit
                ): Future[Any] =
    connectSSE(
      s"${PacmanRestClient.LOBBY_URL}/$id",
      messageHandler,
      connectionErrorHandler,
      !_.eventType.contains(LobbySSEEventType.LOBBY_DELETE.toString)
    )

  private def connectSSE(
                           requestUri: String,
                           messageHandler: String => Unit,
                           connectionErrorHandler: () => Unit,
                           sseEventTypeStop: ServerSentEvent => Boolean
                         ): Future[Any] = {
    val request = Get(requestUri).withHeaders(
      RawHeader("Accept", "text/event-stream")
    )

    sendRequest(request) flatMap { response =>
      response.status match {
        case StatusCodes.OK =>
          Unmarshal(response.entity).to[Source[ServerSentEvent, NotUsed]].foreach(
            _.takeWhile(sseEventTypeStop(_))
              .runForeach(sse => messageHandler(sse.getData())) onComplete {
                case Success(_) => info("SSE chiusa correttamente")
                case Failure(exception) =>
                  info(s"Connessione SSE interrotta ${exception.getMessage}")
                  connectionErrorHandler()
              }
          )
          Future.successful("OK")
        case _ => Unmarshal(response.entity).to[String] flatMap { body =>
          Future.failed(new IOException(s"Errore connessione SSE: $body"))
        }
      }
    }
  }

  /**
   * Invia richiesta creazione lobby
   * @param hostUsername  nome utente di chi crea la lobby
   * @param description descrizione della lobby
   * @param size  numero massimo di giocatori
   * @return  i dati della lobby come JSON
   */
  def createLobby(
                   hostUsername: String,
                   description: String,
                   size: Int
                 ): Future[String] = {
    val request = Post(PacmanRestClient.LOBBY_URL).withEntity(
      HttpEntity(
        ContentTypes.`application/json`,
        JsObject(
          "hostUsername" -> JsString(hostUsername),
          "description" -> JsString(description),
          "size" -> JsNumber(size)
        ).toString()
      )
    )

    sendRequest(request) flatMap { response =>
      response.status match {
        case StatusCodes.Created => Unmarshal(response.entity).to[String]
        case StatusCodes.InternalServerError => Unmarshal(response.entity).to[String] flatMap { body =>
          Future.failed(new IOException(s"Errore creazione lobby: $body"))
        }
        case _ => handleUnknownStatusCode(request, response)
      }
    }
  }

  /**
   * Invia richiesta partecipazione ad una lobby
   * @param lobbyId id della lobby a cui partecipare
   * @param username  username giocatore che vuole partecipare
   * @return  i dati del participant come JSON
   */
  def joinLobby(
                 lobbyId: Int,
                 username: String
               ): Future[String] = {
    val request = Post(PacmanRestClient.PARTICIPANT_URL).withEntity(
      HttpEntity(
        ContentTypes.`application/json`,
        JsObject(
          "lobbyId" -> JsNumber(lobbyId),
          "username" -> JsString(username),
          "pacmanType" -> JsNumber(1)
        ).toString()
      )
    )

    sendRequest(request) flatMap { response =>
      response.status match {
        case StatusCodes.Created => Unmarshal(response.entity).to[String]
        case StatusCodes.InternalServerError => Unmarshal(response.entity).to[String] flatMap { body =>
          Future.failed(new IOException(s"Errore partecipazione lobby: $body"))
        }
        case _ => handleUnknownStatusCode(request, response)
      }
    }
  }

  /**
   * Invia richiesta abbandono lobby
   * @param username  nome giocatore che vuole abbandonare
   * @return  i dati del participant come JSON
   */
  def leaveLobby(username: String): Future[String] = {
    val request = Delete(s"${PacmanRestClient.PARTICIPANT_URL}/$username")

    sendRequest(request) flatMap { response =>
      response.status match {
        case StatusCodes.OK => Unmarshal(response.entity).to[String]
        case StatusCodes.InternalServerError => Unmarshal(response.entity).to[String] flatMap { body =>
          Future.failed(new IOException(s"Errore durante abbandono lobby: $body"))
        }
        case _ => handleUnknownStatusCode(request, response)
      }
    }
  }

  /**
   * Invia richiesta nuova partita
   * @param players il numero di giocatori per questa partita
   * @return l'id della nuova partita
   */
  def startGame(players: Int): Future[String] = {
    // Come passare un JSON https://stackoverflow.com/a/56569369/4328569
    val request = Post(PacmanRestClient.GAMES_URL).withEntity(
      HttpEntity(
        ContentTypes.`application/json`,
        JsObject("playersNumber" -> JsNumber(players)).toString())
    )
    sendRequest(request) flatMap { response =>
      response.status match {
        case StatusCodes.Created => Unmarshal(response.entity).to[String]
        case StatusCodes.InternalServerError => Unmarshal(response.entity).to[String] flatMap { body =>
          Future.failed(new IOException(s"Non è stato possibile creare una nuova partita: $body"))
        }
        case _ => handleUnknownStatusCode(request, response)
      }
    }
  }

  /**
   * Invia richiesta termine partita
   * @param gameId id della partita da terminare
   * @return la risposta del server
   */
  def endGame(gameId: String): Future[String] = {
    val request = Delete(s"${PacmanRestClient.GAMES_URL}/$gameId") // scalastyle:ignore multiple.string.literals
    sendRequest(request) flatMap { response =>
      response.status match {
        case StatusCodes.Accepted => Unmarshal(response.entity).to[String]
        case _ => handleUnknownStatusCode(request, response)
      }
    }
  }

  // scalastyle:off
  /**
   * Apre il canale di comunicazione WebSocket per ricezione aggiornamenti di gioco.
   * Salva il riferimento dell'attore che gestisce il Source per poter inviare informazioni al server
   * tramite la WebSocket
   * @param gameId id della partita
   * @param playerName nickname del giocatore
   * @param serverMessageHandler funzione a cui vengono passati i messaggi del server
   * @param connectionErrorHandler funzione per la gestione della caduta improvvisa della connessione
   */
  // Source: https://stackoverflow.com/questions/40345697/how-to-use-akka-http-client-websocket-send-message
  def openWS(
              gameId: String,
              playerName: String,
              serverMessageHandler: String => Unit,
              connectionErrorHandler: Boolean => Unit,
            ): Unit = {
    val request = WebSocketRequest(s"${PacmanRestClient.GAMES_WS_URL}/$gameId?playerName=$playerName")

    val messageSource: Source[Message, ActorRef] =
      Source.actorRef(
        completionMatcher = {
          // complete stream immediately if we send it Done
          case Done => CompletionStrategy.immediately
        },
        // never fail the stream because of a message
        failureMatcher = PartialFunction.empty,
        bufferSize = WS_BUFFER_SIZE,
        OverflowStrategy.dropHead
      )

    val messageSink: Sink[Message, Future[Done]] = Sink.foreachAsync(1) {
      // Caso per l'esecuzione di server e client in locale
      case tms: TextMessage.Strict => serverMessageHandler(tms.text); Future.successful(Unit);
      // Caso per l'esecuzione di server e client tramite la rete
      case tm: TextMessage => tm.toStrict(FiniteDuration(TEXT_MESSAGE_TO_STRICT, "ms")) map { tms =>
        serverMessageHandler(tms.text)
      }
      case _ => warn("[WebSocket] Ricevuto messaggio non gestito"); Future.successful(Unit);
    }

    val webSocketFlow = establishWebSocket(request)

    val ((webSocketSpeaker, upgradeResponse), closed) =
      messageSource
        .viaMat(webSocketFlow)(Keep.both)
        .toMat(messageSink)(Keep.both)
        .run()

    _webSocketSpeaker = Some(webSocketSpeaker)

    upgradeResponse.flatMap { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        info("[upgradeResponse - Success] Connessione weboscket stabilita correttamente")
        Future.successful(Done)
      } else {
        val msg = s"Connessione fallita: ${upgrade.response.status}"

        info(s"[upgradeResponse - Failure] $msg")

        throw new RuntimeException(msg)
      }
    }

    closed onComplete {
      case Success(_) => closeWebSocket()
      case Failure(exception) =>
        error(s"[closed - Failure] ${exception.getMessage}")

        if (!exception.getMessage.contains("Internal Server Error")) {
          connectionErrorHandler(false)
        } else {
          info("[closed - Failure] Problema con il server, tentativo di riconnessione annullato")
          connectionErrorHandler(true)
          closeWebSocket()
        }
    }
  }

  /**
   * Invia un messaggio al server tramite la WebSocket
   * @param message il messaggio da inviare al server
   */
  def sendOverWebSocket(message: String): Unit =
    if(_webSocketSpeaker.isDefined) _webSocketSpeaker.get ! TextMessage.Strict(message)

  /**
   * Chiude la WebSocket e cancella il riferimento all'attore
   */
  def closeWebSocket(): Unit =
    if(_webSocketSpeaker.isDefined) _webSocketSpeaker.get ! Done; _webSocketSpeaker = None

  private def handleUnknownStatusCode(request: HttpRequest, response: HttpResponse): Future[Nothing] = Unmarshal(response.entity).to[String] flatMap { body =>
    Future.failed(new IOException(s"Stato risposta dal server è ${response.status} [${request.uri}] e il body è $body"))
  }
}
// scalastyle:on multiple.string.literals


case object PacmanRestClient {

  val serverURL: String = appConf.getString("server.address") + ":" +  appConf.getInt("server.port")

  /**
   * Indirizzo per creazione/termine partita
   */
  val GAMES_URL = s"http://$serverURL/games"
  /**
   * Indirizzo per canale WebSocket
   */
  val GAMES_WS_URL = s"ws://$serverURL/connection-management/games"
  /**
   * Indirizzo per lobby
   */
  val LOBBY_URL = s"http://$serverURL/api/lobby"
  /**
   * Indirizzo per participant
   */
  val PARTICIPANT_URL = s"http://$serverURL/api/participant"
}
