package it.unibo.scalapacman.client.communication

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, WebSocketRequest, WebSocketUpgradeResponse}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow

import scala.concurrent.Future

/**
 * Interfaccia con le firme per la comunicazione con il server
 */
trait HttpClient {
  /**
   * Effettua una chiamata HTTP
   * @param httpRequest la richiesta HTTP
   * @param classicActorSystem l'actor system utilizzato da Akka HTTP
   * @return la risposta HTTP
   */
  def sendRequest(httpRequest: HttpRequest)(implicit classicActorSystem: ActorSystem): Future[HttpResponse]

  /**
   * Apre un canale di comunicazione WebSocket con il server
   * @param wsRequest la richiesta WebSocket
   * @param classicActorSystem  l'actor system utilizzato da Akka HTTP
   * @return il canale di comunicazione
   */
  def establishWebSocket(wsRequest: WebSocketRequest)(implicit classicActorSystem: ActorSystem): Flow[Message, Message, Future[WebSocketUpgradeResponse]]
}

/**
 * Interfaccia con le implementazioni delle chiamate utilizzando Akka HTTP
 */
trait ClientHandler extends HttpClient {
  override def sendRequest(httpRequest: HttpRequest)(implicit classicActorSystem: ActorSystem): Future[HttpResponse] = Http().singleRequest(httpRequest)

  override def establishWebSocket(wsRequest: WebSocketRequest)(implicit classicActorSystem: ActorSystem):
    Flow[Message, Message, Future[WebSocketUpgradeResponse]] = Http().webSocketClientFlow(wsRequest)
}
