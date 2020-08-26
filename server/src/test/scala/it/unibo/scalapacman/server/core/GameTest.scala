package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.TestException
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.Message
import it.unibo.scalapacman.server.communication.ConnectionProtocol.{Ack, ConnectionFailed, ConnectionInit}
import it.unibo.scalapacman.server.core.Game.{CloseCommand, RegisterPlayer}
import it.unibo.scalapacman.server.core.Player.{PlayerRegistration, RegistrationAccepted, RegistrationRejected}
import it.unibo.scalapacman.server.config.{ConfLoader, TestSettings}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

class GameTest extends ScalaTestWithActorTestKit(ConfLoader.config) with AnyWordSpecLike with BeforeAndAfterEach {

  private var gameActor: ActorRef[Game.GameCommand] = _
  private var fooProbe: TestProbe[Message] = _

  override def beforeEach(): Unit = {
    gameActor = spawn(Game("TestActorGame"))
    fooProbe = createTestProbe[Message]()
  }

  "A Master actor" must {

    "handle player registration" in {

      val testProbe = createTestProbe[PlayerRegistration]()
      val exMsg = "Test connessione caduta"

      gameActor ! RegisterPlayer(testProbe.ref, fooProbe.ref)
      testProbe.receiveMessage() match {
        case RegistrationAccepted(messageHandler) =>
          messageHandler ! ConnectionInit(TestProbe[Ack].ref)
          messageHandler ! ConnectionFailed(TestException(exMsg))
        case _ => fail()
      }

      //Attendo che player elabori il messaggio ConnectionFailed
      Thread.sleep(TestSettings.waitTime.toMillis)

      gameActor ! RegisterPlayer(testProbe.ref, fooProbe.ref)
      testProbe.receiveMessage() match {
        case RegistrationAccepted(_) =>
        case _ => fail()
      }
    }

    "reject registration during game" in {

      val testProbe = createTestProbe[PlayerRegistration]()

      gameActor ! RegisterPlayer(testProbe.ref, fooProbe.ref)
      testProbe.receiveMessage() match {
        case RegistrationAccepted(_) =>
        case _ => fail()
      }

      gameActor ! RegisterPlayer(testProbe.ref, fooProbe.ref)
      testProbe.receiveMessage() match {
        case RegistrationRejected(_) =>
        case _ => fail()
      }
    }

    "is stoppable" in {
      gameActor ! CloseCommand()
      TestProbe().expectTerminated(gameActor)
    }
  }
}
