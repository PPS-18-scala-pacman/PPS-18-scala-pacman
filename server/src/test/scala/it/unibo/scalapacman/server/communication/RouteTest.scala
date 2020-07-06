package it.unibo.scalapacman.server.communication

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import it.unibo.scalapacman.server.core.Master
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// https://github.com/akka/akka-http/issues/3030
class RouteTest extends AnyWordSpec with ScalatestRouteTest with Matchers {

  var routes: Route = _
  var probeRoutesHandler: TestProbe[ServiceRoutes.RoutesCommand] = _
  var mockedRoutesHandler: ActorRef[ServiceRoutes.RoutesCommand] = _
  var testKit: ActorTestKit = _

  val testGameId: String = Master.gameIdPrefix format 0

  override def beforeAll(): Unit = {
    super.beforeAll()

    testKit = ActorTestKit()
    implicit val actorSystem: ActorSystem[Nothing] = testKit.system

    val mockedBehavior = Behaviors.receiveMessage[ServiceRoutes.RoutesCommand] {
      case ServiceRoutes.CreateGame(replyTo) =>
        replyTo ! ServiceRoutes.OK(testGameId)
        Behaviors.same
    }
    probeRoutesHandler = testKit.createTestProbe[ServiceRoutes.RoutesCommand]()
    mockedRoutesHandler = testKit.spawn(Behaviors.monitor(probeRoutesHandler.ref, mockedBehavior))
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

    "return a MethodNotAllowed error for PUT requests to the game path" in {
      Put("/games") ~> Route.seal(routes) ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
      }
    }

    "return a game for GET requests to the game path" in {
      Post("/games") ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)
        responseAs[String] shouldEqual testGameId
      }
      probeRoutesHandler.receiveMessage() match {
        case ServiceRoutes.CreateGame(_) =>
        case _ => fail()
      }
    }

    "delete a game for DELETE requests to the game path" in {
      val res = Delete(s"/games/$testGameId") ~> routes

      probeRoutesHandler.expectMessage(ServiceRoutes.DeleteGame(testGameId))

      res ~> check {
        status should ===(StatusCodes.Accepted)
      }
    }
  }
}
