package it.unibo.scalapacman.server.core;

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import it.unibo.scalapacman.lib.model.GhostType
import it.unibo.scalapacman.server.util.Settings
import org.scalatest.wordspec.AnyWordSpecLike

class EngineUpdateTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val fakeGameId = "fakeCreateGameId"

  private var engineActor: ActorRef[Engine.EngineCommand] = _
  private var watcherPlayerProbe: TestProbe[Engine.UpdateCommand] = _

  override def beforeAll(): Unit = {
    engineActor = spawn(Engine(fakeGameId))
    watcherPlayerProbe = createTestProbe[Engine.UpdateCommand]()
    val watcherFooProbe = createTestProbe[Engine.UpdateCommand]()
    GhostType.values.foreach(engineActor ! Engine.RegisterGhost(watcherFooProbe.ref, _))
  }

  "An Engine actor" must {
    "update watcher after registration" in {
      engineActor ! Engine.RegisterPlayer(watcherPlayerProbe.ref)

      watcherPlayerProbe.receiveMessage(Settings.gameRefreshRate * 2) match {
        case Engine.UpdateMsg(_) =>
        case _ => fail()
      }
    }
  }
}
