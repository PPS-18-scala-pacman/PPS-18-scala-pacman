package it.unibo.scalapacman.client.communication

import java.io.IOException

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.client.RequestBuilding.{Delete, Post}
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Sink, Source}
import grizzled.slf4j.Logging

import scala.concurrent.{ExecutionContextExecutor, Future}

trait PacmanRestClient extends Logging { this: HttpClient =>
  implicit def classicActorSystem: ActorSystem
  implicit def executionContext: ExecutionContextExecutor

  val WS_BUFFER_SIZE: Int = 1

  var _webSocketSpeaker: Option[ActorRef] = None

  def startGame: Future[String] = {
    val request = Post(PacmanRestClient.GAMES_URL)
    sendRequest(request) flatMap { response =>
      response.status match {
        case StatusCodes.Created => Unmarshal(response.entity).to[String]
        case StatusCodes.InternalServerError => Unmarshal(response.entity).to[String] flatMap { body =>
          Future.failed(new IOException(s"Non è stato possibile creare una nuova partita: $body")) // scalastyle:ignore multiple.string.literals
        }
        case _ => handleUnknownStatusCode(request, response)
      }
    }
  }

  def endGame(gameId: String): Future[String] = {
    val request = Delete(s"${PacmanRestClient.GAMES_URL}/$gameId") // scalastyle:ignore multiple.string.literals
    sendRequest(request) flatMap { response =>
      response.status match {
        case StatusCodes.Accepted => Unmarshal(response.entity).to[String]
        case _ => handleUnknownStatusCode(request, response)
      }
    }
  }

  // Source: https://stackoverflow.com/questions/40345697/how-to-use-akka-http-client-websocket-send-message
  def openWS(gameId: String, serverMessageHandler: String => Unit): Unit = {
    val request = WebSocketRequest(s"${PacmanRestClient.GAMES_WS_URL}/$gameId")

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

    val messageSink: Sink[Message, Future[Done]] = Sink.foreach[Message] {
      case tms: TextMessage.Strict => serverMessageHandler(tms.text)
      case _ => Unit
    }

    val webSocketFlow = establishWebSocket(request)

    val ((webSocketSpeaker, upgradeResponse), closed) =
      messageSource
        .viaMat(webSocketFlow)(Keep.both)
        .toMat(messageSink)(Keep.both)
        .run()

    _webSocketSpeaker = Some(webSocketSpeaker)

    // TODO connected da usare?
//    val connected = upgradeResponse.flatMap { upgrade =>
    upgradeResponse.flatMap { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Future.successful(Done)
      } else {
        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      }
    }

    // TODO da passare anche questo sopra? Se la websocket chiude, vuol dire che anche il gioco deve chiudersi?
    closed.foreach(thing => debug(s"WebSocket chiusa: $thing"))
  }

  def sendOverWebSocket(message: String): Unit =
    if(_webSocketSpeaker.isDefined) _webSocketSpeaker.get ! TextMessage.Strict(message)

  def closeWebSocket(): Unit =
    if(_webSocketSpeaker.isDefined) _webSocketSpeaker.get ! Done; _webSocketSpeaker = None

  private def handleUnknownStatusCode(request: HttpRequest, response: HttpResponse): Future[Nothing] = Unmarshal(response.entity).to[String] flatMap { body =>
    Future.failed(new IOException(s"Stato risposta dal server è ${response.status} [${request.uri}] e il body è $body"))
  }
}

case object PacmanRestClient {
  val GAMES_URL = "http://localhost:8080/games"
  val GAMES_WS_URL = "ws://localhost:8080/connection-management/games"
}
