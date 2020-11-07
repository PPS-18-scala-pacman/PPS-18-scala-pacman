package it.unibo.scalapacman.client.communication

import java.io.IOException

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.http.scaladsl.model.ws.{Message, WebSocketRequest, WebSocketUpgradeResponse}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import org.scalamock.function.MockFunction1
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import spray.json.DefaultJsonProtocol.{IntJsonFormat, StringJsonFormat, mapFormat}
import spray.json.enrichAny

import scala.concurrent.{ExecutionContextExecutor, Future}

class PacmanRestClientTest
  extends ScalaTestWithActorTestKit
    with AsyncWordSpecLike
    with Matchers
    with ScalaFutures
    with MockFactory {

  trait MockClientHandler extends HttpClient {
    val mockHttp: MockFunction1[HttpRequest, Future[HttpResponse]] = mockFunction[HttpRequest, Future[HttpResponse]]
    val mockWS: MockFunction1[WebSocketRequest, Flow[Message, Message, Future[WebSocketUpgradeResponse]]] =
      mockFunction[WebSocketRequest, Flow[Message, Message, Future[WebSocketUpgradeResponse]]]

    override def sendRequest(httpRequest: HttpRequest)(implicit classicActorSystem: ActorSystem): Future[HttpResponse] =
      mockHttp(httpRequest)

    override def establishWebSocket(wsRequest: WebSocketRequest)(implicit classicActorSystem: ActorSystem):
      Flow[Message, Message, Future[WebSocketUpgradeResponse]] = mockWS(wsRequest)
  }

  class PacmanRestClientWithMockClientHandler extends PacmanRestClient with MockClientHandler {
    // Nella nuova suite di testkit viene utilizzato akka.actor.typed, ma akka-http ha ancora bisogno del classico
    override implicit def classicActorSystem: ActorSystem = testKit.system.classicSystem
    override implicit def executionContext: ExecutionContextExecutor = classicActorSystem.dispatcher
  }

  private var pacmanRestClient: PacmanRestClientWithMockClientHandler = _
  private val GAME_ID_EXAMPLE = "1"
  private val FAILURE_MESSAGE = "Failure message"
  private val NUM_PLAYERS: Int = 1
  private val START_GAME_REQUEST_ENTITY = HttpEntity(ContentTypes.`application/json`, Map("playersNumber" -> NUM_PLAYERS).toJson.toString())

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    pacmanRestClient = new PacmanRestClientWithMockClientHandler()
  }

  "Pacman Rest Client" must {

    "handle create game success" in {
      val expectedGameId = GAME_ID_EXAMPLE

      pacmanRestClient.mockHttp
        .expects(HttpRequest(method = HttpMethods.POST, uri = PacmanRestClient.GAMES_URL, entity = START_GAME_REQUEST_ENTITY))
        .returning(Future.successful(HttpResponse(status = StatusCodes.Created, entity = HttpEntity(ByteString(expectedGameId)))))

      whenReady(pacmanRestClient.startGame(NUM_PLAYERS)) { res =>
        res shouldEqual expectedGameId
      }
    }

    "handle create game failure" in {
      val failureMessage = FAILURE_MESSAGE

      pacmanRestClient.mockHttp
        .expects(HttpRequest(method = HttpMethods.POST, uri = PacmanRestClient.GAMES_URL, entity = START_GAME_REQUEST_ENTITY))
        .returning(Future.successful(HttpResponse(status = StatusCodes.InternalServerError, entity = HttpEntity(ByteString(FAILURE_MESSAGE)))))

      recoverToSucceededIf[IOException] {
        pacmanRestClient.startGame(NUM_PLAYERS) flatMap { res =>
          res shouldEqual failureMessage
        }
      }
    }

    "handle create game unknown response" in {
      val failureMessage = FAILURE_MESSAGE

      pacmanRestClient.mockHttp
        .expects(HttpRequest(method = HttpMethods.POST, uri = PacmanRestClient.GAMES_URL, entity = START_GAME_REQUEST_ENTITY))
        .returning(Future.successful(HttpResponse(status = StatusCodes.NotFound, entity = HttpEntity(ByteString(failureMessage)))))

      recoverToSucceededIf[IOException] {
        pacmanRestClient.startGame(NUM_PLAYERS) flatMap { res =>
          res shouldEqual failureMessage
        }
      }
    }

    "handle delete game request success" in {
      val gameId = GAME_ID_EXAMPLE
      val uri = s"${PacmanRestClient.GAMES_URL}/$gameId"

      val expectedMessage = "Delete request received"

      pacmanRestClient.mockHttp
        .expects(HttpRequest(method = HttpMethods.DELETE, uri = uri))
        .returning(Future.successful(HttpResponse(status = StatusCodes.Accepted, entity = HttpEntity(ByteString(expectedMessage)))))

      whenReady(pacmanRestClient endGame gameId) { res =>
        res shouldEqual expectedMessage
      }
    }

    "handle delete game unknown response" in {
      val gameId = GAME_ID_EXAMPLE
      val uri = s"${PacmanRestClient.GAMES_URL}/$gameId"

      val failureMessage = FAILURE_MESSAGE

      pacmanRestClient.mockHttp
        .expects(HttpRequest(method = HttpMethods.DELETE, uri = uri))
        .returning(Future.successful(HttpResponse(status = StatusCodes.InternalServerError, entity = HttpEntity(ByteString(failureMessage)))))

      recoverToSucceededIf[IOException] {
        pacmanRestClient.endGame(gameId) flatMap { res =>
          res shouldEqual failureMessage
        }
      }
    }
  }
}
