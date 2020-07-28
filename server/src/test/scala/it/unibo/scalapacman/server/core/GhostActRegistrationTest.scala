package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import it.unibo.scalapacman.lib.model.GhostType
import org.scalatest.wordspec.AnyWordSpecLike

class GhostActRegistrationTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "A Ghost actor" must {

    "register himself at startup" in {
      val engineProbe = createTestProbe[Engine.EngineCommand]()
      val fakeGameId = "fakeCreateGameId"
      val ghostType = GhostType.BLINKY

      val ghostActor: ActorRef[Engine.UpdateCommand] = spawn(GhostAct(fakeGameId, engineProbe.ref, ghostType))

      engineProbe.receiveMessage() match {
        case Engine.RegisterGhost(`ghostActor`, `ghostType`) =>
        case _ => fail()
      }
    }
  }
}
