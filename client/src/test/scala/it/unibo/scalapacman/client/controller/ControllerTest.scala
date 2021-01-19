package it.unibo.scalapacman.client.controller

import java.awt.event.KeyEvent
import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.ws.{Message, WebSocketRequest, WebSocketUpgradeResponse}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.communication.{HttpClient, PacmanRestClient}
import it.unibo.scalapacman.client.controller.Action.{END_GAME, MOVEMENT, PAUSE_RESUME, RESET_KEY_MAP, SAVE_KEY_MAP}
import it.unibo.scalapacman.client.input.JavaKeyBinding.DefaultJavaKeyBinding
import it.unibo.scalapacman.client.input.KeyMap
import it.unibo.scalapacman.client.utils.PacmanLogger
import it.unibo.scalapacman.common.{CommandType, MoveCommandType}
import it.unibo.scalapacman.lib.model.{Map, MapType}
import org.scalamock.function.MockFunction1
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

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
                           ): Future[Any] = Future.successful("OK")
  }

  var _defaultKeyMap: KeyMap = _
  var _notDefaultKeyMap: KeyMap = _
  var _pacmanRestClientWithMockClientHandler: PacmanRestClientWithMockClientHandler = _
  var _logger: PacmanLogger = _
  var _controller: Controller = _
  val GAME_ID = "fakeID"
  val MAP_CLASSIC: Map = Map.create(MapType.CLASSIC)

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    _defaultKeyMap = KeyMap(DefaultJavaKeyBinding.UP, DefaultJavaKeyBinding.DOWN, DefaultJavaKeyBinding.RIGHT,
      DefaultJavaKeyBinding.LEFT, DefaultJavaKeyBinding.PAUSE)
    _notDefaultKeyMap = KeyMap(KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_A, KeyEvent.VK_O)
    _pacmanRestClientWithMockClientHandler = new PacmanRestClientWithMockClientHandler()
    _logger = new PacmanLoggerTest()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    _controller = Controller(_pacmanRestClientWithMockClientHandler, _logger)
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

      // TODO test sulle lobby

      "be able to end a game" in {
        _controller.model = _controller.model.copy(gameId = Some(GAME_ID))
        _controller.model.gameId shouldEqual Some(GAME_ID)

        _controller.handleAction(END_GAME, None)

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
    }
  }
}
