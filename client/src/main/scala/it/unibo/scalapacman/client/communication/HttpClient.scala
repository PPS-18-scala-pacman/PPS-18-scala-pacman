package it.unibo.scalapacman.client.communication

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, WebSocketRequest, WebSocketUpgradeResponse}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow

import scala.concurrent.Future

trait HttpClient {
  def sendRequest(httpRequest: HttpRequest)(implicit classicActorSystem: ActorSystem): Future[HttpResponse]

  def establishWebSocket(wsRequest: WebSocketRequest)(implicit classicActorSystem: ActorSystem): Flow[Message, Message, Future[WebSocketUpgradeResponse]]
}

trait ClientHandler extends HttpClient {
  override def sendRequest(httpRequest: HttpRequest)(implicit classicActorSystem: ActorSystem): Future[HttpResponse] = Http().singleRequest(httpRequest)

  override def establishWebSocket(wsRequest: WebSocketRequest)(implicit classicActorSystem: ActorSystem):
    Flow[Message, Message, Future[WebSocketUpgradeResponse]] = Http().webSocketClientFlow(wsRequest)
}
