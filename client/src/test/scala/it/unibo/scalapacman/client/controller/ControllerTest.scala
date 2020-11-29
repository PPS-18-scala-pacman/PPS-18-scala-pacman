package it.unibo.scalapacman.client.controller

import java.awt.event.KeyEvent

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.ws.{Message, WebSocketRequest, WebSocketUpgradeResponse}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.communication.{HttpClient, PacmanRestClient}
import it.unibo.scalapacman.client.controller.Action.{END_GAME, MOVEMENT, PAUSE_RESUME, RESET_KEY_MAP, SAVE_KEY_MAP, START_GAME}
import it.unibo.scalapacman.client.input.JavaKeyBinding.DefaultJavaKeyBinding
import it.unibo.scalapacman.client.input.KeyMap
import it.unibo.scalapacman.client.model.CreateLobbyData
import it.unibo.scalapacman.common.{CommandType, MoveCommandType}
import it.unibo.scalapacman.lib.model.{Map, MapType}
import org.scalamock.function.MockFunction1
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import spray.json.DefaultJsonProtocol.{IntJsonFormat, StringJsonFormat, mapFormat}
import spray.json.{JsNumber, JsObject, enrichAny}

import scala.concurrent.{ExecutionContextExecutor, Future}

class ControllerTest
  extends ScalaTestWithActorTestKit
  with AnyWordSpecLike
  with MockFactory
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with Matchers {

  trait MockClientHandler extends HttpClient {
    val mockHttp: MockFunction1[HttpRequest, Future[HttpResponse]] = mockFunction[HttpRequest, Future[HttpResponse]]

    override def sendRequest(httpRequest: HttpRequest)(implicit classicActorSystem: ActorSystem): Future[HttpResponse] =
      mockHttp(httpRequest)

    // Non mocko questo metodo perché al momento non so come fare
    override def establishWebSocket(wsRequest: WebSocketRequest)(implicit classicActorSystem: ActorSystem):
    Flow[Message, Message, Future[WebSocketUpgradeResponse]] = Http().webSocketClientFlow(wsRequest)
  }

  class PacmanRestClientWithMockClientHandler extends PacmanRestClient with MockClientHandler with Logging {
    // Nella nuova suite di testkit viene utilizzato akka.actor.typed, ma akka-http ha ancora bisogno del classico
    override implicit def classicActorSystem: ActorSystem = testKit.system.classicSystem
    override implicit def executionContext: ExecutionContextExecutor = classicActorSystem.dispatcher

    // Non mi serve inviare effettivamente al server per il test
    override def sendOverWebSocket(message: String): Unit = debug(s"Messaggio da inviare $message")// scalastyle:ignore
    override def connectSSE(
                             requestUri: String,
                             messageHandler: ServerSentEvent => Unit,
                             connectionErrorHandler: () => Unit,
                             onSSEClose: () => Unit,
                             sseEventTypeStop: ServerSentEvent => Boolean
                           ): Future[Any] = Future.successful()
  }

  var _defaultKeyMap: KeyMap = _
  var _notDefaultKeyMap: KeyMap = _
  var _pacmanRestClientWithMockClientHandler: PacmanRestClientWithMockClientHandler = _
  var _controller: Controller = _
  val GAME_ID = "fakeID"
  val MAP_CLASSIC: Map = Map.create(MapType.CLASSIC)

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    _defaultKeyMap = KeyMap(DefaultJavaKeyBinding.UP, DefaultJavaKeyBinding.DOWN, DefaultJavaKeyBinding.RIGHT,
      DefaultJavaKeyBinding.LEFT, DefaultJavaKeyBinding.PAUSE)
    _notDefaultKeyMap = KeyMap(KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_A, KeyEvent.VK_O)
    _pacmanRestClientWithMockClientHandler = new PacmanRestClientWithMockClientHandler()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    _controller = Controller(_pacmanRestClientWithMockClientHandler)
  }

  "Controller" when {
    "instantiated" must {
      "have default key mapping" in {
        _controller.model.keyMap shouldEqual _defaultKeyMap
      }

      "not have a saved user action" in {
        _controller.userAction shouldEqual None
      }
    }

    "handling user action" must {
      // 29/11/2020: Caso non reale
//      "be able to start a new game" in {
//        val NUM_PLAYERS: Int = 1
//        val START_GAME_REQUEST_ENTITY = HttpEntity(
//          ContentTypes.`application/json`,
//          JsObject("playersNumber" -> JsNumber(NUM_PLAYERS)).toString()
//        )
//        val cgd: CreateLobbyData = CreateLobbyData("test", NUM_PLAYERS)
//
//        _controller.model.gameId shouldEqual None
//
//        _pacmanRestClientWithMockClientHandler.mockHttp
//          .expects(HttpRequest(method = HttpMethods.POST, uri = PacmanRestClient.GAMES_URL, entity = START_GAME_REQUEST_ENTITY))
//          .returning(Future.successful(HttpResponse(status = StatusCodes.Created, entity = HttpEntity(ByteString(GAME_ID)))))
//
//        _controller.handleAction(START_GAME, Some(cgd))
//
//        // TODO si riesce a cambiare metodo?
//        // Attendo molto per dare tempo alla websocket(?) poiché non so come mockarla?
//        Thread.sleep(500)// scalastyle:ignore
//
//        _controller.model.gameId shouldEqual Some(GAME_ID)
//      }
//
//      "be able to handle failure when starting a new game" in {
//        val FAILURE_MESSAGE: String = "failure"
//        val NUM_PLAYERS: Int = 1
//        val START_GAME_REQUEST_ENTITY = HttpEntity(
//          ContentTypes.`application/json`,
//          scala.collection.immutable.Map("playersNumber" -> NUM_PLAYERS).toJson.toString()
//        )
//        val cgd: CreateLobbyData = CreateLobbyData("test", NUM_PLAYERS)
//
//        _controller.model.gameId shouldEqual None
//
//        _pacmanRestClientWithMockClientHandler.mockHttp
//          .expects(HttpRequest(method = HttpMethods.POST, uri = PacmanRestClient.GAMES_URL, entity = START_GAME_REQUEST_ENTITY))
//          .returning(Future.successful(HttpResponse(status = StatusCodes.InternalServerError, entity = HttpEntity(ByteString(FAILURE_MESSAGE)))))
//
//        _controller.handleAction(START_GAME, Some(cgd))
//
//        // TODO si riesce a cambiare metodo?
//        Thread.sleep(100)// scalastyle:ignore
//
//        _controller.model.gameId shouldEqual None
//      }

      "not start a new game when one is already on" in {
        _controller.model = _controller.model.copy(gameId = Some(GAME_ID))

        _controller.handleAction(START_GAME, None)
        _controller.model.gameId shouldEqual Some(GAME_ID)
      }

      "be able to end a game when request is successful" in {
        _controller.model = _controller.model.copy(gameId = Some(GAME_ID))
        _controller.model.gameId shouldEqual Some(GAME_ID)

        val uri = s"${PacmanRestClient.GAMES_URL}/$GAME_ID"
        val expectedMessage = "Delete request received"

        _pacmanRestClientWithMockClientHandler.mockHttp
          .expects(HttpRequest(method = HttpMethods.DELETE, uri = uri))
          .returning(Future.successful(HttpResponse(status = StatusCodes.Accepted, entity = HttpEntity(ByteString(expectedMessage)))))

        _controller.handleAction(END_GAME, None)

        // TODO si riesce a cambiare metodo?
        Thread.sleep(100)// scalastyle:ignore

        _controller.model.gameId shouldEqual None
      }

      "be able to end a game when request is not successful" in {
        _controller.model = _controller.model.copy(gameId = Some(GAME_ID))
        _controller.model.gameId shouldEqual Some(GAME_ID)

        val uri = s"${PacmanRestClient.GAMES_URL}/$GAME_ID"
        val failureMessage = "failure"

        _pacmanRestClientWithMockClientHandler.mockHttp
          .expects(HttpRequest(method = HttpMethods.DELETE, uri = uri))
          .returning(Future.successful(HttpResponse(status = StatusCodes.InternalServerError, entity = HttpEntity(ByteString(failureMessage)))))

        _controller.handleAction(END_GAME, None)

        // TODO si riesce a cambiare metodo?
        Thread.sleep(100)// scalastyle:ignore

        _controller.model.gameId shouldEqual None
      }

      "not end a game when there is no one on" in {
        _controller.model.gameId shouldEqual None

        _controller.handleAction(END_GAME, None)
        _controller.model.gameId shouldEqual None
      }

      "be able to save a valid key mapping" in {
        _controller.model.keyMap shouldEqual _defaultKeyMap

        _controller.handleAction(SAVE_KEY_MAP, Some(_notDefaultKeyMap))
        _controller.model.keyMap shouldEqual _notDefaultKeyMap
      }

      "not save a non-valid key mapping" in {
        _controller.model.keyMap shouldEqual _defaultKeyMap

        _controller.handleAction(SAVE_KEY_MAP, None)
        _controller.model.keyMap shouldEqual _defaultKeyMap
      }

      "be able to reset key mapping" in {
        _controller.model.keyMap shouldEqual _defaultKeyMap

        _controller.handleAction(SAVE_KEY_MAP, Some(_notDefaultKeyMap))
        _controller.model.keyMap shouldEqual _notDefaultKeyMap

        _controller.handleAction(RESET_KEY_MAP, None)
        _controller.model.keyMap shouldEqual _defaultKeyMap
      }

      "be able to save a user action while a game is on" in {
        _controller.userAction shouldEqual None

        _controller.model = _controller.model.copy(gameId = Some(GAME_ID))

        _controller.handleAction(MOVEMENT, Some(MoveCommandType.UP))
        _controller.userAction shouldEqual Some(MoveCommandType.UP)

        _controller.handleAction(MOVEMENT, Some(MoveCommandType.DOWN))
        _controller.userAction shouldEqual Some(MoveCommandType.DOWN)

        _controller.handleAction(MOVEMENT, Some(MoveCommandType.RIGHT))
        _controller.userAction shouldEqual Some(MoveCommandType.RIGHT)

        _controller.handleAction(MOVEMENT, Some(MoveCommandType.LEFT))
        _controller.userAction shouldEqual Some(MoveCommandType.LEFT)

        _controller.handleAction(MOVEMENT, Some(MoveCommandType.NONE))
        _controller.userAction shouldEqual Some(MoveCommandType.NONE)
      }

      "handle same user action while a game is on" in {
        _controller.userAction shouldEqual None

        _controller.model = _controller.model.copy(gameId = Some(GAME_ID))

        _controller.handleAction(MOVEMENT, Some(MoveCommandType.NONE))
        _controller.userAction shouldEqual Some(MoveCommandType.NONE)

        _controller.handleAction(MOVEMENT, Some(MoveCommandType.NONE))
        _controller.userAction shouldEqual Some(MoveCommandType.NONE)
      }

      "handle missing user action" in {
        _controller.userAction shouldEqual None

        _controller.model = _controller.model.copy(gameId = Some(GAME_ID))

        _controller.handleAction(MOVEMENT, Some(MoveCommandType.NONE))
        _controller.userAction shouldEqual Some(MoveCommandType.NONE)

        _controller.handleAction(MOVEMENT, None)
        _controller.userAction shouldEqual Some(MoveCommandType.NONE)
      }

      "not save a user action when a game is not on" in {
        _controller.userAction shouldEqual None

        _controller.handleAction(MOVEMENT, Some(MoveCommandType.UP))
        _controller.userAction shouldEqual None

        _controller.handleAction(MOVEMENT, Some(MoveCommandType.DOWN))
        _controller.userAction shouldEqual None

        _controller.handleAction(MOVEMENT, Some(MoveCommandType.RIGHT))
        _controller.userAction shouldEqual None

        _controller.handleAction(MOVEMENT, Some(MoveCommandType.LEFT))
        _controller.userAction shouldEqual None

        _controller.handleAction(MOVEMENT, Some(MoveCommandType.NONE))
        _controller.userAction shouldEqual None
      }

      "be able to pause game when game is on and not paused" in {
        _controller.model = _controller.model.copy(gameId = Some(GAME_ID))

        _controller.model.paused shouldEqual false

        _controller.handleAction(PAUSE_RESUME, Some(CommandType.PAUSE))
        _controller.model.paused shouldEqual true
      }

      "do nothing when pause is requested and game is already paused" in {
        _controller.model = _controller.model.copy(gameId = Some(GAME_ID), paused = true)

        _controller.model.paused shouldEqual true

        _controller.handleAction(PAUSE_RESUME, Some(CommandType.PAUSE))
        _controller.model.paused shouldEqual true
      }

      "be able to resume game when game is on and paused" in {
        _controller.model = _controller.model.copy(gameId = Some(GAME_ID), paused = true)

        _controller.model.paused shouldEqual true

        _controller.handleAction(PAUSE_RESUME, Some(CommandType.RESUME))
        _controller.model.paused shouldEqual false
      }

      "do nothing when resume is requested and game is not paused" in {
        _controller.model = _controller.model.copy(gameId = Some(GAME_ID), paused = false)

        _controller.model.paused shouldEqual false

        _controller.handleAction(PAUSE_RESUME, Some(CommandType.RESUME))
        _controller.model.paused shouldEqual false
      }

      "not pausing/resuming when a game is not on" in {
        _controller.model.paused shouldEqual false

        _controller.handleAction(PAUSE_RESUME, Some(CommandType.PAUSE))
        _controller.model.paused shouldEqual false

        _controller.model = _controller.model.copy(paused = true)
        _controller.model.paused shouldEqual true

        _controller.handleAction(PAUSE_RESUME, Some(CommandType.RESUME))
        _controller.model.paused shouldEqual true
      }

      "handle missing pause/resume value" in {
        _controller.model = _controller.model.copy(gameId = Some(GAME_ID))

        _controller.model.paused shouldEqual false

        _controller.handleAction(PAUSE_RESUME, None)
        _controller.model.paused shouldEqual false

        _controller.model = _controller.model.copy(paused = true)

        _controller.model.paused shouldEqual true

        _controller.handleAction(PAUSE_RESUME, None)
        _controller.model.paused shouldEqual true
      }
    }
  }
}
