package it.unibo.scalapacman.server.communication

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import it.unibo.scalapacman.lib.model.PacmanType
import it.unibo.scalapacman.server.communication.ConnectionProtocol.ConnectionMsg
import it.unibo.scalapacman.server.core.Game.GameCommand
import it.unibo.scalapacman.server.core.PlayerAct.{RegistrationAccepted, RegistrationRejected}
import it.unibo.scalapacman.server.core.{Game, Master}
import it.unibo.scalapacman.server.config.TestSettings.askTestDuration
import it.unibo.scalapacman.server.model.GameComponent
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

        serviceHandlerActor ! ServiceRoutes.CreateGame(clientProbe.ref, List(GameComponent("fooId", PacmanType.PACMAN)))
        masterProbe.expectMessageType[Master.CreateGame]

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
        val fooProbe = createTestProbe[ConnectionMsg]()
        val testId = "testId"

        serviceHandlerActor ! ServiceRoutes.CreateConnectionGame(clientProbe.ref, fakeGameId, testId)
        gameProbe.receiveMessage() match {
          case Game.RegisterPlayer(replyTo, _, `testId`) => replyTo ! RegistrationAccepted(fooProbe.ref)
          case _ => fail()
        }

        clientProbe.expectMessageType[ServiceRoutes.SuccessConG]
      }
    }

    "handle a reject for create connection for game request" when {
      "requested" in {
        val clientProbe = createTestProbe[ServiceRoutes.ResponseConnGame]()
        val errMsg: String = "err:RegistrationRejected"
        val testId = "testId"

        serviceHandlerActor ! ServiceRoutes.CreateConnectionGame(clientProbe.ref, fakeGameId, testId)
        gameProbe.receiveMessage() match {
          case Game.RegisterPlayer(replyTo, _, `testId`) => replyTo ! RegistrationRejected(errMsg)
          case _ => fail()
        }

        clientProbe.expectMessage(ServiceRoutes.FailureConG(errMsg))
      }
    }

    "handle a missing response for create connection for game request" when {
      "missing" in {
        val clientProbe = createTestProbe[ServiceRoutes.ResponseConnGame]()

        serviceHandlerActor ! ServiceRoutes.CreateConnectionGame(clientProbe.ref, fakeGameId, "fooId")
        gameProbe.expectMessageType[Game.RegisterPlayer]

        clientProbe.expectMessageType[ServiceRoutes.FailureConG](askTestDuration)
      }
    }
  }
}
