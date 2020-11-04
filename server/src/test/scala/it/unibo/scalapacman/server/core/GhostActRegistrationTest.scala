package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.{ActorRef, MailboxSelector}
import it.unibo.scalapacman.lib.model.GhostType
import it.unibo.scalapacman.server.config.ConfLoader
import org.scalatest.wordspec.AnyWordSpecLike

class GhostActRegistrationTest extends ScalaTestWithActorTestKit(ConfLoader.akkaConf) with AnyWordSpecLike {

  "A Ghost actor" must {

    "register himself at startup" in {
      val engineProbe = createTestProbe[Engine.EngineCommand]()
      val fakeGameId = "fakeCreateGameId"
      val ghostType = GhostType.BLINKY

      val props = MailboxSelector.fromConfig("ghost-mailbox")
      val ghostActor: ActorRef[Engine.UpdateCommand] = spawn(GhostAct(fakeGameId, engineProbe.ref, ghostType), props)

      engineProbe.expectMessage(Engine.RegisterGhost(ghostActor, ghostType))
    }
  }
}
