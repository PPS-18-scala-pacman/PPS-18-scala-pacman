package it.unibo.scalapacman.server.communication

import akka.NotUsed
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.ws.{BinaryMessage, Message}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import it.unibo.scalapacman.server.core.Master
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// https://github.com/akka/akka-http/issues/3030
class RouteTest extends AnyWordSpec with ScalatestRouteTest with Matchers {

  var routes: Route = _
  var probeRoutesHandler: TestProbe[ServiceRoutes.RoutesCommand] = _
  var mockedRoutesHandler: ActorRef[ServiceRoutes.RoutesCommand] = _
  var probeWSClient: WSProbe = _
  var testKit: ActorTestKit = _

  val testGameId: String = Master.gameIdPrefix format 0
  val failureTestGameId: String = Master.gameIdPrefix format 1

  val failureTestMsg:String = "err:FailureConG"

  override def beforeAll(): Unit = {
    super.beforeAll()

    testKit = ActorTestKit()
    implicit val actorSystem: ActorSystem[Nothing] = testKit.system

    val mockedBehavior = Behaviors.receiveMessage[ServiceRoutes.RoutesCommand] {
      case ServiceRoutes.CreateGame(replyTo, _) =>
        replyTo ! ServiceRoutes.SuccessCrG(testGameId)
        Behaviors.same
      case ServiceRoutes.CreateConnectionGame(replyTo, `testGameId`, _) =>
        val echoFlow: Flow[Message, Message, NotUsed] =  Flow[Message]
        replyTo ! ServiceRoutes.SuccessConG(echoFlow)
        Behaviors.same
      case ServiceRoutes.CreateConnectionGame(replyTo, `failureTestGameId`, _) =>
        replyTo ! ServiceRoutes.FailureConG(failureTestMsg)
        Behaviors.same
      case _ =>
        Behaviors.same
    }
    probeRoutesHandler = testKit.createTestProbe[ServiceRoutes.RoutesCommand]()
    mockedRoutesHandler = testKit.spawn(Behaviors.monitor(probeRoutesHandler.ref, mockedBehavior))
    probeWSClient = WSProbe()
    routes = ServiceRoutes(mockedRoutesHandler)
  }

  override def afterAll(): Unit = {
    super.afterAll()

    testKit.shutdownTestKit()
  }

  "The service" should {

    "leave GET requests to other paths unhandled" in {
      Get("/foo") ~> routes ~> check {
        handled shouldBe false
      }
    }

    "return a MethodNotAllowed error for PUT requests to the games path" in {
      Put("/games") ~> Route.seal(routes) ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
      }
    }

    "return a new game for POST requests to the games path" in {
      Post("/games", HttpEntity(ContentTypes.`application/json`, """{"components":{"nickname1":1,"nickname2":2}}""")) ~>
        routes ~> check {
          status shouldEqual StatusCodes.Created
          contentType shouldEqual ContentTypes.`text/plain(UTF-8)`
          responseAs[String] shouldEqual testGameId
        }
        probeRoutesHandler.receiveMessage() match {
          case ServiceRoutes.CreateGame(_, _) =>
          case _ => fail()
        }
    }

    "delete a game for DELETE requests to the games path" in {
      val res = Delete(s"/games/$testGameId") ~> routes //scalastyle:ignore

      probeRoutesHandler.expectMessage(ServiceRoutes.DeleteGame(testGameId))

      res ~> check {
        status should ===(StatusCodes.Accepted)
      }
    }

    "accept ws connection" in {
      WS(s"/connection-management/games/$testGameId?playerName=fooId", probeWSClient.flow) ~> routes ~> check {
        isWebSocketUpgrade shouldEqual true

        probeWSClient.sendMessage("echo")
        probeWSClient.expectMessage("echo")

        probeWSClient.sendMessage(BinaryMessage(ByteString("abcdef")))
        probeWSClient.expectNoMessage()
      }
    }

    "decline ws connection" in {
      WS(s"/connection-management/games/$failureTestGameId?playerName=fooId", probeWSClient.flow) ~> routes ~> check {
        isWebSocketUpgrade shouldEqual false

        status shouldEqual StatusCodes.InternalServerError
        responseAs[String] shouldEqual failureTestMsg
      }
    }
  }
}
