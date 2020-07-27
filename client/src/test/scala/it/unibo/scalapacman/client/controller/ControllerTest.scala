package it.unibo.scalapacman.client.controller

import java.awt.event.KeyEvent

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.ws.{Message, WebSocketRequest, WebSocketUpgradeResponse}
import akka.stream.scaladsl.Flow
import grizzled.slf4j.Logging
import it.unibo.scalapacman.client.communication.{HttpClient, PacmanRestClient}
import it.unibo.scalapacman.client.controller.Action.{MOVEMENT, MOVE_DEFAULT, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT, MOVE_UP, RESET_KEY_MAP, SAVE_KEY_MAP}
import it.unibo.scalapacman.client.input.JavaKeyBinding.DefaultJavaKeyBinding
import it.unibo.scalapacman.client.input.KeyMap
import org.scalamock.function.MockFunction1
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.{ExecutionContextExecutor, Future}

class ControllerTest
  extends ScalaTestWithActorTestKit
  with AnyWordSpecLike
  with MockFactory
  with BeforeAndAfterEach {


  trait MockClientHandler extends HttpClient {
    val mockHttp: MockFunction1[HttpRequest, Future[HttpResponse]] = mockFunction[HttpRequest, Future[HttpResponse]]

    override def sendRequest(httpRequest: HttpRequest)(implicit classicActorSystem: ActorSystem): Future[HttpResponse] =
      mockHttp(httpRequest)

    override def establishWebSocket(wsRequest: WebSocketRequest)(implicit classicActorSystem: ActorSystem):
    Flow[Message, Message, Future[WebSocketUpgradeResponse]] = Http().webSocketClientFlow("ws://echo.websocket.org")
  }

  class PacmanRestClientWithMockClientHandler extends PacmanRestClient with MockClientHandler with Logging {
    // Nella nuova suite di testkit viene utilizzato akka.actor.typed, ma akka-http ha ancora bisogno del classico
    override implicit def classicActorSystem: ActorSystem = testKit.system.classicSystem
    override implicit def executionContext: ExecutionContextExecutor = classicActorSystem.dispatcher

    // Non mi serve inviare effettivamente al server per il test
    override def sendOverWebSocket(message: String): Unit = debug(s"Messaggio da inviare $message")
  }

  val _defaultKeyMap: KeyMap = KeyMap(DefaultJavaKeyBinding.UP, DefaultJavaKeyBinding.DOWN, DefaultJavaKeyBinding.RIGHT, DefaultJavaKeyBinding.LEFT)
  val _notDefaultKeyMap: KeyMap = KeyMap(KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_A)
  val _pacmanRestClientWithMockClientHandler = new PacmanRestClientWithMockClientHandler()
  var _controller: Controller = _

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    _controller = Controller(_pacmanRestClientWithMockClientHandler)
  }

  "Controller" when {
    "instantiated" must {
      "have default key mapping" in {
        assertResult(_defaultKeyMap)(_controller.getKeyMap)
      }

      "not have a key mapping different from the default one" in {
        assert(_controller.getKeyMap != _notDefaultKeyMap)
      }

      "not have a saved user action" in {
        assertResult(None)(_controller.getUserAction)
      }
    }

    "handling user action" must {
      "be able to save a new valid key mapping" in {
        assertResult(_defaultKeyMap)(_controller.getKeyMap)

        _controller.handleAction(SAVE_KEY_MAP, Some(_notDefaultKeyMap))
        assertResult(_notDefaultKeyMap)(_controller.getKeyMap)
      }

      "not save a new non-valid key mapping" in {
        assertResult(_defaultKeyMap)(_controller.getKeyMap)

        _controller.handleAction(SAVE_KEY_MAP, None)
        assertResult(_defaultKeyMap)(_controller.getKeyMap)
      }

      "be able to reset key mapping" in {
        assertResult(_defaultKeyMap)(_controller.getKeyMap)

        _controller.handleAction(SAVE_KEY_MAP, Some(_notDefaultKeyMap))
        assertResult(_notDefaultKeyMap)(_controller.getKeyMap)

        _controller.handleAction(RESET_KEY_MAP, None)
        assertResult(_defaultKeyMap)(_controller.getKeyMap)
      }

      "be able to save a new valid user action" in {
        assertResult(None)(_controller.getUserAction)

        _controller.handleAction(MOVEMENT, Some(MOVE_UP))
        assertResult(Some(MOVE_UP))(_controller.getUserAction)

        _controller.handleAction(MOVEMENT, Some(MOVE_DOWN))
        assertResult(Some(MOVE_DOWN))(_controller.getUserAction)

        _controller.handleAction(MOVEMENT, Some(MOVE_RIGHT))
        assertResult(Some(MOVE_RIGHT))(_controller.getUserAction)

        _controller.handleAction(MOVEMENT, Some(MOVE_LEFT))
        assertResult(Some(MOVE_LEFT))(_controller.getUserAction)

        _controller.handleAction(MOVEMENT, Some(MOVE_DEFAULT))
        assertResult(Some(MOVE_DEFAULT))(_controller.getUserAction)
      }
    }
  }
}