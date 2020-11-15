package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.TestException
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.Message
import it.unibo.scalapacman.server.communication.ConnectionProtocol.{Ack, ConnectionAck, ConnectionEnded, ConnectionFailed, ConnectionInit, ConnectionMsg}
import it.unibo.scalapacman.server.core.Engine.EngineCommand
import it.unibo.scalapacman.server.core.PlayerAct.PlayerCommand
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

class PlayerConnectionTest extends ScalaTestWithActorTestKit with AnyWordSpecLike with BeforeAndAfterEach {

  private var playerActor     : ActorRef[PlayerCommand] = _
  private var playerCmdAdapter: ActorRef[ConnectionMsg] = _
  private var engineProbe     : TestProbe[EngineCommand] = _
  private var ackProbe        : TestProbe[Ack] = _

  override def beforeAll(): Unit = {
    ackProbe = createTestProbe[Ack]()
    engineProbe = createTestProbe[EngineCommand]()
  }

  override def beforeEach(): Unit = {
    // creazione e registrazione attore player
    val fakeGameId = "fakeCreateGameId"
    val regReqSender = createTestProbe[PlayerAct.PlayerRegistration]()
    val clientProbe = createTestProbe[Message]()
    playerActor = spawn(PlayerAct(fakeGameId, engineProbe.ref))

    playerActor ! PlayerAct.RegisterUser(regReqSender.ref, clientProbe.ref, "playerId")
    engineProbe.expectMessageType[Engine.RegisterWatcher]

    regReqSender.receiveMessage() match {
      case PlayerAct.RegistrationAccepted(ref) => playerCmdAdapter = ref
      case _ => fail()
    }
  }

  "A Player actor" must {

    "acknowledge" when {
      "connection is initialized" in {

        playerCmdAdapter ! ConnectionInit(ackProbe.ref)
        receiveAck()
      }

      "connection fails" in {
        playerCmdAdapter ! ConnectionInit(ackProbe.ref)
        receiveAck()

        playerCmdAdapter ! ConnectionEnded()
        TestProbe().expectTerminated(playerActor)
      }

      "connection is closed" in {
        val exMsg = "Test connessione caduta"
        playerCmdAdapter ! ConnectionInit(ackProbe.ref)
        receiveAck()

        playerCmdAdapter ! ConnectionFailed(TestException(exMsg))
        TestProbe().expectTerminated(playerActor)
      }
    }
  }

  private def receiveAck(): Unit = ackProbe.expectMessageType[ConnectionAck]
}
