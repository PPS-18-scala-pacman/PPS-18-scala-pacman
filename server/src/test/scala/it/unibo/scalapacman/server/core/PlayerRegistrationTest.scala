package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.Message
import org.scalatest.wordspec.AnyWordSpecLike

class PlayerRegistrationTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  private var playerActor: ActorRef[Player.PlayerCommand] = _

  override def beforeAll(): Unit = {
    val engineProbe = createTestProbe[Engine.EngineCommand]()
    val fakeGameId = "fakeCreateGameId"
    playerActor = spawn(Player(fakeGameId, engineProbe.ref))
  }

  "A Player actor" must {

    "accept a registration user request" in {
      val regReqSender = createTestProbe[Player.PlayerRegistration]()
      val clientProbe = createTestProbe[Message]()

      playerActor ! Player.RegisterUser(regReqSender.ref, clientProbe.ref)
      regReqSender.expectMessageType[Player.RegistrationAccepted]
    }

    "reject a second registration user request" in {
      val regReqSenderFst = createTestProbe[Player.PlayerRegistration]()
      val regReqSenderSnd = createTestProbe[Player.PlayerRegistration]()
      val clientProbe = createTestProbe[Message]()

      playerActor ! Player.RegisterUser(regReqSenderFst.ref, clientProbe.ref)
      regReqSenderFst.receiveMessage()

      playerActor ! Player.RegisterUser(regReqSenderSnd.ref, clientProbe.ref)
      regReqSenderSnd.expectMessageType[Player.RegistrationRejected]
    }
  }
}
