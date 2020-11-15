package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.Message
import org.scalatest.wordspec.AnyWordSpecLike

class PlayerRegistrationTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  private var playerActor: ActorRef[PlayerAct.PlayerCommand] = _

  override def beforeAll(): Unit = {
    val engineProbe = createTestProbe[Engine.EngineCommand]()
    val fakeGameId = "fakeCreateGameId"
    playerActor = spawn(PlayerAct(fakeGameId, engineProbe.ref))
  }

  "A Player actor" must {

    "accept a registration user request" in {
      val regReqSender = createTestProbe[PlayerAct.PlayerRegistration]()
      val clientProbe = createTestProbe[Message]()

      playerActor ! PlayerAct.RegisterUser(regReqSender.ref, clientProbe.ref, "playerId")
      regReqSender.expectMessageType[PlayerAct.RegistrationAccepted]
    }

    "reject a second registration user request" in {
      val regReqSenderFst = createTestProbe[PlayerAct.PlayerRegistration]()
      val regReqSenderSnd = createTestProbe[PlayerAct.PlayerRegistration]()
      val clientProbe = createTestProbe[Message]()

      playerActor ! PlayerAct.RegisterUser(regReqSenderFst.ref, clientProbe.ref, "playerId")
      regReqSenderFst.receiveMessage()

      playerActor ! PlayerAct.RegisterUser(regReqSenderSnd.ref, clientProbe.ref, "playerId")
      regReqSenderSnd.expectMessageType[PlayerAct.RegistrationRejected]
    }
  }
}
