package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import it.unibo.scalapacman.server.util.ConfLoader
import org.scalatest.wordspec.AnyWordSpecLike

class GameTest extends ScalaTestWithActorTestKit(ConfLoader.config) with AnyWordSpecLike {

  private var gameActor: ActorRef[Game.GameCommand] = _

  override def beforeAll(): Unit = {
    gameActor = spawn(Game("TestActorGame"))
  }

  "A Master actor" must {

    "is stoppable" in {
      gameActor ! Game.CloseCommand()
      TestProbe().expectTerminated(gameActor)
    }
  }
}
