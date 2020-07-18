package it.unibo.scalapacman.client.communication

import java.io.IOException

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding.{Delete, Post}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import grizzled.slf4j.Logging

import scala.concurrent.{ExecutionContextExecutor, Future}

trait PacmanRestClient extends Logging { this: HttpClient =>
  implicit def classicActorSystem: ActorSystem
  implicit def executionContext: ExecutionContextExecutor

  def startGame: Future[String] = {
    val request = Post(PacmanRestClient.GAME_URL)
    sendRequest(request) flatMap { response =>
      response.status match {
        case StatusCodes.OK => Unmarshal(response.entity).to[String]
        case StatusCodes.InternalServerError => Unmarshal(response.entity).to[String] flatMap { body =>
          Future.failed(new IOException(s"Non è stato possibile creare una nuova partita: $body")) // scalastyle:ignore multiple.string.literals
        }
        case _ => handleUnknownStatusCode(request, response)
      }
    }
  }

  def endGame(gameId: String): Future[String] = {
    val request = Delete(s"${PacmanRestClient.GAME_URL}/$gameId")
    sendRequest(request) flatMap { response =>
      response.status match {
        case StatusCodes.Accepted => Unmarshal(response.entity).to[String]
        case _ => handleUnknownStatusCode(request, response)
      }
    }
  }

  private def handleUnknownStatusCode(request: HttpRequest, response: HttpResponse): Future[Nothing] = Unmarshal(response.entity).to[String] flatMap { body =>
    Future.failed(new IOException(s"Stato risposta dal server è ${response.status} [${request.uri}] e il body è $body"))
  }
}

case object PacmanRestClient {
  val GAME_URL = "http://localhost:8080/games"
}
