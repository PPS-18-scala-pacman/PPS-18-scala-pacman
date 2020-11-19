package it.unibo.scalapacman.server.core

import akka.actor.InvalidActorNameException
import akka.actor.testkit.typed.Effect.{Spawned, Watched}
import akka.actor.testkit.typed.TestException
import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.{ActorRef, ChildFailed}
import akka.http.scaladsl.model.ws.Message
import it.unibo.scalapacman.lib.model.GhostType
import it.unibo.scalapacman.lib.model.PacmanType.PACMAN
import it.unibo.scalapacman.server.communication.ConnectionProtocol.{Ack, ConnectionFailed, ConnectionInit}
import it.unibo.scalapacman.server.config.TestSettings.waitTime
import it.unibo.scalapacman.server.core.Game.{CloseCommand, RegisterPlayer}
import it.unibo.scalapacman.server.core.PlayerAct.{PlayerRegistration, RegistrationAccepted, RegistrationRejected}
import it.unibo.scalapacman.server.config.{ConfLoader, TestSettings}
import it.unibo.scalapacman.server.model.GameComponent
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

class GameTest extends ScalaTestWithActorTestKit(ConfLoader.akkaConf) with AnyWordSpecLike with BeforeAndAfterEach {

  val fakeGameId = "TestActorGame"
  val testId = "TestId"

  private var gameActor: ActorRef[Game.GameCommand] = _
  private var fooProbe: TestProbe[Message] = _

  override def beforeEach(): Unit = {
    gameActor = spawn(Game(fakeGameId, List(GameComponent(testId, PACMAN))))
    fooProbe = createTestProbe[Message]()
  }

  "A Game actor" must {

    "handle player registration" in {

      val testProbe = createTestProbe[PlayerRegistration]()
      val exMsg = "Test connessione caduta"

      gameActor ! RegisterPlayer(testProbe.ref, fooProbe.ref, testId)
      testProbe.receiveMessage() match {
        case RegistrationAccepted(messageHandler) =>
          messageHandler ! ConnectionInit(TestProbe[Ack].ref)
          messageHandler ! ConnectionFailed(TestException(exMsg))
        case _ => fail()
      }

      //Attendo che player elabori il messaggio ConnectionFailed
      Thread.sleep(TestSettings.waitTime.toMillis)

      gameActor ! RegisterPlayer(testProbe.ref, fooProbe.ref, testId)
      testProbe.expectMessageType[RegistrationAccepted](waitTime)
    }

    "create a new ghost" when {
      "ghost child fails" in {
        val testKit = BehaviorTestKit(Game(fakeGameId, List(GameComponent(testId, PACMAN)), visible = false))
        val blinkyAct = testKit.childInbox("BLINKYActor").ref

        assertThrows[InvalidActorNameException](testKit.signal(ChildFailed(blinkyAct, TestException("testException"))))
      }
    }

    "create actor for game" in {
      val testKit = BehaviorTestKit(Game(fakeGameId, List(GameComponent(testId, PACMAN)), visible = false))

      testKit.expectEffectType[Spawned[Engine]]
      GhostType.values.foreach(_ => testKit.expectEffectType[Spawned[GhostAct]])

      testKit.expectEffectType[Watched[Engine]]
      GhostType.values.foreach(_ => testKit.expectEffectType[Watched[GhostAct]])
    }

    "reject registration during game" in {

      val testProbe = createTestProbe[PlayerRegistration]()

      gameActor ! RegisterPlayer(testProbe.ref, fooProbe.ref, testId)
      testProbe.expectMessageType[RegistrationAccepted](waitTime)

      gameActor ! RegisterPlayer(testProbe.ref, fooProbe.ref, testId)
      testProbe.expectMessageType[RegistrationRejected](waitTime)
    }

    "is stoppable" in {
      gameActor ! CloseCommand()
      TestProbe().expectTerminated(gameActor)
    }
  }
}
