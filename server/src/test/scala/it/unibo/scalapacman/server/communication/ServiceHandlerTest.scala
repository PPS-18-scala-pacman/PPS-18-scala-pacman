package it.unibo.scalapacman.server.communication

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.http.scaladsl.model.ws.Message
import it.unibo.scalapacman.server.core.Game.GameCommand
import it.unibo.scalapacman.server.core.Player.{RegistrationAccepted, RegistrationRejected}
import it.unibo.scalapacman.server.core.{Game, Master}
import it.unibo.scalapacman.server.util.TestSettings
import org.scalatest.wordspec.AnyWordSpecLike

class ServiceHandlerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val fakeGameId = "fakeCreateGameId"

  private var serviceHandlerActor: ActorRef[ServiceRoutes.RoutesCommand] = _
  private var gameProbe: TestProbe[Game.GameCommand] = _

  override def beforeAll(): Unit = {
    serviceHandlerActor = spawn(ServiceHandler())

    gameProbe = createTestProbe[Game.GameCommand]()
    val gameServiceKey: ServiceKey[GameCommand] = ServiceKey[GameCommand](fakeGameId)
    system.receptionist ! Receptionist.Register(gameServiceKey, gameProbe.ref)
  }

  "A ServiceHandler actor" must {

    "send a create a new game request" when {
      "asked" in {
        val clientProbe = createTestProbe[ServiceRoutes.ResponseCreateGame]()
        val masterProbe = createTestProbe[Master.MasterCommand]()
        system.receptionist ! Receptionist.Register(Master.masterServiceKey, masterProbe.ref)

        serviceHandlerActor ! ServiceRoutes.CreateGame(clientProbe.ref)
        masterProbe.receiveMessage() match {
          case Master.CreateGame(_) =>
          case _ => fail()
        }

        serviceHandlerActor ! ServiceHandler.WrapRespCreateGame(Master.GameCreated(fakeGameId))
        clientProbe.expectMessage(ServiceRoutes.SuccessCrG(fakeGameId))
      }
    }

    "send a delete game request" when {
      "asked" in {

        serviceHandlerActor ! ServiceRoutes.DeleteGame(fakeGameId)
        gameProbe.expectMessage(Game.CloseCommand())
      }
    }

    "send a create connection for game request" when {
      "requested" in {
        val clientProbe = createTestProbe[ServiceRoutes.ResponseConnGame]()
        val fooProbe = createTestProbe[Message]()

        serviceHandlerActor ! ServiceRoutes.CreateConnectionGame(clientProbe.ref, fakeGameId)
        gameProbe.receiveMessage() match {
          case Game.RegisterPlayer(replyTo, _) => replyTo ! RegistrationAccepted(fooProbe.ref)
          case _ => fail()
        }

        clientProbe.receiveMessage() match {
          case ServiceRoutes.SuccessConG(_) =>
          case _ => fail()
        }
      }
    }

    "handle a reject for create connection for game request" when {
      "requested" in {
        val clientProbe = createTestProbe[ServiceRoutes.ResponseConnGame]()
        val errMsg: String = "err:RegistrationRejected"

        serviceHandlerActor ! ServiceRoutes.CreateConnectionGame(clientProbe.ref, fakeGameId)
        gameProbe.receiveMessage() match {
          case Game.RegisterPlayer(replyTo, _) => replyTo ! RegistrationRejected(errMsg)
          case _ => fail()
        }

        clientProbe.receiveMessage() match {
          case ServiceRoutes.FailureConG(`errMsg`) =>
          case _ => fail()
        }
      }
    }

    "handle a missing response for create connection for game request" when {
      "missing" in {
        val clientProbe = createTestProbe[ServiceRoutes.ResponseConnGame]()

        serviceHandlerActor ! ServiceRoutes.CreateConnectionGame(clientProbe.ref, fakeGameId)
        gameProbe.receiveMessage() match {
          case Game.RegisterPlayer(_, _) =>
          case _ => fail()
        }

        clientProbe.receiveMessage(TestSettings.askTestDuration) match {
          case ServiceRoutes.FailureConG(_) =>
          case _ => fail()
        }
      }
    }
  }
}
