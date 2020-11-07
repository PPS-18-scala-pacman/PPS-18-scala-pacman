package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import it.unibo.scalapacman.common.GameCharacter
import it.unibo.scalapacman.common.GameCharacter.PACMAN
import it.unibo.scalapacman.lib.model.GhostType
import it.unibo.scalapacman.lib.model.GhostType.BLINKY
import it.unibo.scalapacman.server.config.ConfLoader
import it.unibo.scalapacman.server.config.TestSettings.waitTime
import it.unibo.scalapacman.server.core.Engine.{ActorRecovery, Pause, RegisterGhost, RegisterPlayer, Resume}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

class EngineRecoveryTest extends ScalaTestWithActorTestKit(ConfLoader.akkaConf) with AnyWordSpecLike with BeforeAndAfterEach {

  val fakeGameId = "fakeCreateGameId"

  private var engineActor: ActorRef[Engine.EngineCommand] = _
  private var watcherPlayerProbe: TestProbe[Engine.UpdateCommand] = _
  private var watcherBlinkyProbe: TestProbe[Engine.UpdateCommand] = _

  override def beforeEach(): Unit = {
    engineActor = spawn(Engine(fakeGameId, 1))
    watcherPlayerProbe = createTestProbe[Engine.UpdateCommand]()
    watcherBlinkyProbe = createTestProbe[Engine.UpdateCommand]()
    val fooWatcherProbe = createTestProbe[Engine.UpdateCommand]()

    engineActor ! Engine.RegisterPlayer(watcherPlayerProbe.ref)
    GhostType.values.filter(_ != BLINKY).foreach(gt => engineActor ! Engine.RegisterGhost(fooWatcherProbe.ref, gt.asInstanceOf[GhostType.GhostType]))

    engineActor ! Engine.RegisterGhost(watcherBlinkyProbe.ref, BLINKY)

    engineActor ! Engine.Resume()
  }

  "An Engine actor" must {

    "handle actor Player crash" in {

      expectateUpdateMsg(watcherPlayerProbe)

      engineActor ! ActorRecovery(PACMAN)

      //Attendo che venga elaborato il messaggio di recovery
      Thread.sleep(waitTime.toMillis)

      watcherPlayerProbe.expectNoMessage(waitTime)

      val newWatcherProbe = createTestProbe[Engine.UpdateCommand]()
      engineActor ! RegisterPlayer(newWatcherProbe.ref)

      expectateUpdateMsg(newWatcherProbe)
      watcherPlayerProbe.expectNoMessage(waitTime)

      engineActor ! Resume()

      expectateUpdateMsg(newWatcherProbe)
      watcherPlayerProbe.expectNoMessage(waitTime)
    }

    "handle actor Ghost crash" in {

      expectateUpdateMsg(watcherBlinkyProbe)

      engineActor ! ActorRecovery(GameCharacter.BLINKY)

      //Attendo che venga elaborato il messaggio di recovery
      Thread.sleep(waitTime.toMillis)

      watcherBlinkyProbe.expectNoMessage(waitTime)

      val newWatcherProbe = createTestProbe[Engine.UpdateCommand]()
      engineActor ! RegisterGhost(newWatcherProbe.ref, BLINKY)

      expectateUpdateMsg(newWatcherProbe)
      watcherBlinkyProbe.expectNoMessage(waitTime)

      engineActor ! Resume()

      expectateUpdateMsg(newWatcherProbe)
      watcherBlinkyProbe.expectNoMessage(waitTime)
    }

    "handle multiple actor crash" in {

      expectateUpdateMsg(watcherBlinkyProbe)
      expectateUpdateMsg(watcherPlayerProbe)

      engineActor ! ActorRecovery(GameCharacter.BLINKY)
      engineActor ! ActorRecovery(PACMAN)

      //Attendo che venga elaborato il messaggio di recovery
      Thread.sleep(waitTime.toMillis)

      watcherBlinkyProbe.expectNoMessage(waitTime)
      watcherPlayerProbe.expectNoMessage(waitTime)

      val newWatcherBlinkyProbe = createTestProbe[Engine.UpdateCommand]()
      engineActor ! RegisterGhost(newWatcherBlinkyProbe.ref, BLINKY)

      newWatcherBlinkyProbe.expectNoMessage(waitTime)
      watcherBlinkyProbe.expectNoMessage(waitTime)
      watcherPlayerProbe.expectNoMessage(waitTime)

      engineActor ! Resume()

      newWatcherBlinkyProbe.expectNoMessage(waitTime)
      watcherBlinkyProbe.expectNoMessage(waitTime)
      watcherPlayerProbe.expectNoMessage(waitTime)

      val newWatcherPlayerProbe = createTestProbe[Engine.UpdateCommand]()
      engineActor ! RegisterPlayer(newWatcherPlayerProbe.ref)

      engineActor ! Resume()

      expectateUpdateMsg(newWatcherBlinkyProbe)
      expectateUpdateMsg(newWatcherPlayerProbe)
      watcherBlinkyProbe.expectNoMessage(waitTime)
      watcherPlayerProbe.expectNoMessage(waitTime)
    }

    "handle crash during Pause" in {

      expectateUpdateMsg(watcherPlayerProbe)

      engineActor ! Pause()
      engineActor ! ActorRecovery(PACMAN)

      //Attendo che venga elaborato il messaggio di recovery
      Thread.sleep(waitTime.toMillis)

      watcherPlayerProbe.expectNoMessage(waitTime)

      val newWatcherProbe = createTestProbe[Engine.UpdateCommand]()
      engineActor ! RegisterPlayer(newWatcherProbe.ref)

      expectateUpdateMsg(newWatcherProbe)
      watcherPlayerProbe.expectNoMessage(waitTime)

      engineActor ! Resume()

      expectateUpdateMsg(newWatcherProbe)
      watcherPlayerProbe.expectNoMessage(waitTime)
    }
  }

  private def expectateUpdateMsg(act: TestProbe[Engine.UpdateCommand]): Unit = act.expectMessageType[Engine.UpdateMsg]
}
