package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import it.unibo.scalapacman.lib.model.{GhostType, PacmanType}
import it.unibo.scalapacman.server.config.TestSettings.waitTime
import it.unibo.scalapacman.server.model.GameEntity
import org.scalatest.wordspec.AnyWordSpecLike

class EngineUpdateTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val fakeGameId = "fakeCreateGameId"

  private var engineActor: ActorRef[Engine.EngineCommand] = _
  private var watcherPlayerProbe: TestProbe[Engine.UpdateCommand] = _

  override def beforeAll(): Unit = {
    val gameEntities = List(GameEntity(PacmanType.PACMAN.toString(), PacmanType.PACMAN),
      GameEntity(GhostType.BLINKY.toString(), GhostType.BLINKY),
      GameEntity(GhostType.INKY.toString(), GhostType.INKY),
      GameEntity(GhostType.PINKY.toString(), GhostType.PINKY),
      GameEntity(GhostType.CLYDE.toString(), GhostType.CLYDE))

    engineActor = spawn(Engine(fakeGameId, gameEntities, 1))
    watcherPlayerProbe = createTestProbe[Engine.UpdateCommand]()
    val watcherFooProbe = createTestProbe[Engine.UpdateCommand]()
    GhostType.values.foreach(_ => engineActor ! Engine.RegisterWatcher(watcherFooProbe.ref))
  }

  "An Engine actor" must {
    "update watcher after registration" in {
      engineActor ! Engine.RegisterWatcher(watcherPlayerProbe.ref)

      engineActor ! Engine.Start()

      watcherPlayerProbe.expectMessageType[Engine.UpdateMsg](waitTime)
    }
  }
}
