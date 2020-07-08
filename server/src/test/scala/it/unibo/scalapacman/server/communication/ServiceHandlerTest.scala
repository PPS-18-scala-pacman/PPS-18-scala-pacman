package it.unibo.scalapacman.server.communication

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import it.unibo.scalapacman.server.core.Game.GameCommand
import it.unibo.scalapacman.server.core.{Game, Master}
import org.scalatest.wordspec.AnyWordSpecLike

class ServiceHandlerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  private var serviceHandlerActor: ActorRef[ServiceRoutes.RoutesCommand] = _

  override def beforeAll(): Unit = {
    serviceHandlerActor = spawn(ServiceHandler())
  }

  "A ServiceHandler actor" must {

    "send a create a new game request" when {
      "asked" in {
        val fakeGameId = "fakeCreateGameId"
        val clientProbe = createTestProbe[ServiceRoutes.Response]()
        val masterProbe = createTestProbe[Master.MasterCommand]()
        system.receptionist ! Receptionist.Register(Master.masterServiceKey, masterProbe.ref)

        serviceHandlerActor ! ServiceRoutes.CreateGame(clientProbe.ref)
        masterProbe.receiveMessage() match {
          case Master.CreateGame(_) =>
          case _ => fail()
        }

        serviceHandlerActor ! ServiceHandler.WrappedResponseCreateGame(Master.GameCreated(fakeGameId))
        clientProbe.expectMessage(ServiceRoutes.Success(fakeGameId))
      }
    }
    "send a delete game request" when {
      "asked" in {
        val fakeGameId = "fakeDeleteGameId"
        val gameProbe = createTestProbe[Game.GameCommand]()
        val gameServiceKey: ServiceKey[GameCommand] = ServiceKey[GameCommand](fakeGameId)
        system.receptionist ! Receptionist.Register(gameServiceKey, gameProbe.ref)

        serviceHandlerActor ! ServiceRoutes.DeleteGame(fakeGameId)
        gameProbe.expectMessage(Game.CloseCommand())
      }
    }
  }
}
