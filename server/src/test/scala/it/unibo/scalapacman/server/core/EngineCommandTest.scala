package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import it.unibo.scalapacman.lib.model.GhostType
import it.unibo.scalapacman.server.config.TestSettings
import org.scalatest.wordspec.AnyWordSpecLike

class EngineCommandTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val fakeGameId = "fakeCreateGameId"

  private var engineActor: ActorRef[Engine.EngineCommand] = _
  private var watcherPlayerProbe: TestProbe[Engine.UpdateCommand] = _

  override def beforeAll(): Unit = {
    engineActor = spawn(Engine(fakeGameId, 1))
    watcherPlayerProbe = createTestProbe[Engine.UpdateCommand]()

    engineActor ! Engine.RegisterPlayer(watcherPlayerProbe.ref)
    GhostType.values.foreach(engineActor ! Engine.RegisterGhost(watcherPlayerProbe.ref, _))
  }

  "An Engine actor" must {
    "stops after pause command" in {

      engineActor ! Engine.Pause()

      watcherPlayerProbe.expectNoMessage(TestSettings.waitTime)
    }

    "resume game after resume command" in {

      engineActor ! Engine.Pause()
      //attendo che il gioco sia messo in pausa
      Thread.sleep(TestSettings.waitTime.toMillis)

      engineActor ! Engine.Resume()
      watcherPlayerProbe.receiveMessage(TestSettings.waitTime) match {
        case Engine.UpdateMsg(_) =>
        case _ => fail()
      }
    }
  }
}
