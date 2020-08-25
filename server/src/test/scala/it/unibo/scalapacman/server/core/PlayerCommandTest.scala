package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import it.unibo.scalapacman.server.communication.ConnectionProtocol.{Ack, ConnectionAck, ConnectionData, ConnectionInit, ConnectionMsg}
import it.unibo.scalapacman.server.core.Engine.{EngineCommand, UpdateCommand}
import it.unibo.scalapacman.server.model.MoveDirection
import org.scalatest.wordspec.AnyWordSpecLike

class PlayerCommandTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  private var playerCmdAdapter: ActorRef[ConnectionMsg] = _
  private var playerUpdAdapter: ActorRef[UpdateCommand] = _
  private var engineProbe     : TestProbe[EngineCommand] = _
  private var ackProbe        : TestProbe[Ack] = _

  private var testCommandPauseJSON        : String = _
  private var testCommandResumeJSON       : String = _
  private var testCommandUPJSON           : String = _
  private var testCommandDOWNJSON         : String = _
  private var testCommandLEFTJSON         : String = _
  private var testCommandRIGHTJSON        : String = _
  private var testCommandNONEJSON         : String = _
  private var testCommandFakeCmdJSON      : String = _
  private var testCommandFakeDataCmdJSON  : String = _

  override def beforeAll(): Unit = {
    // creazione e registrazione attore player
    ackProbe = createTestProbe[Ack]()
    engineProbe = createTestProbe[EngineCommand]()
    val fakeGameId = "fakeCreateGameId"
    val playerActor = spawn(Player(fakeGameId, engineProbe.ref))
    val regReqSender = createTestProbe[Player.PlayerRegistration]()
    val clientProbe = createTestProbe[Message]()

    playerActor ! Player.RegisterUser(regReqSender.ref, clientProbe.ref)
    engineProbe.receiveMessage() match {
      case Engine.RegisterPlayer(updateRef) => playerUpdAdapter = updateRef
      case _ => fail()
    }
    regReqSender.receiveMessage() match {
      case Player.RegistrationAccepted(ref) => playerCmdAdapter = ref
      case _ => fail()
    }

    playerCmdAdapter ! ConnectionInit(ackProbe.ref)
    receiveAck()

    //mockup comandi
    testCommandPauseJSON        = "{\"id\":{\"commandType\":\"PAUSE\"},\"data\":null}"
    testCommandResumeJSON       = "{\"id\":{\"commandType\":\"RESUME\"},\"data\":null}"
    testCommandUPJSON           = "{\"id\":{\"commandType\":\"MOVE\"},\"data\":\"{\\\"moveCommandType\\\":\\\"UP\\\"}\"}"
    testCommandDOWNJSON         = "{\"id\":{\"commandType\":\"MOVE\"},\"data\":\"{\\\"moveCommandType\\\":\\\"DOWN\\\"}\"}"
    testCommandLEFTJSON         = "{\"id\":{\"commandType\":\"MOVE\"},\"data\":\"{\\\"moveCommandType\\\":\\\"LEFT\\\"}\"}"
    testCommandRIGHTJSON        = "{\"id\":{\"commandType\":\"MOVE\"},\"data\":\"{\\\"moveCommandType\\\":\\\"RIGHT\\\"}\"}"
    testCommandNONEJSON         = "{\"id\":{\"commandType\":\"MOVE\"},\"data\":\"{\\\"moveCommandType\\\":\\\"NONE\\\"}\"}"
    testCommandFakeCmdJSON      = "{\"id\":{\"commandType\":\"FAKE\"},\"data\":null}"
    testCommandFakeDataCmdJSON  = "{\"id\":{\"commandType\":\"MOVE\"},\"data\":null}"
  }

  "A Player actor" must {

    "handle Pause command" in {
      playerCmdAdapter ! ConnectionData(ackProbe.ref, TextMessage(testCommandPauseJSON))
      engineProbe.receiveMessage() match {
        case Engine.Pause() =>
        case _ => fail()
      }
      receiveAck()
    }

    "handle Resume command" in {
      playerCmdAdapter ! ConnectionData(ackProbe.ref, TextMessage(testCommandResumeJSON))
      engineProbe.receiveMessage() match {
        case Engine.Resume() =>
        case _ => fail()
      }
      receiveAck()
    }

    "handle Move Up command" in {
      playerCmdAdapter ! ConnectionData(ackProbe.ref, TextMessage(testCommandUPJSON))
      engineProbe.receiveMessage() match {
        case Engine.ChangeDirectionReq(playerUpRef, MoveDirection.UP) => playerUpRef shouldEqual playerUpdAdapter
        case _ => fail()
      }
      receiveAck()
    }

    "handle Move Down command" in {
      playerCmdAdapter ! ConnectionData(ackProbe.ref, TextMessage(testCommandDOWNJSON))
      engineProbe.receiveMessage() match {
        case Engine.ChangeDirectionReq(playerUpRef, MoveDirection.DOWN) => playerUpRef shouldEqual playerUpdAdapter
        case _ => fail()
      }
      receiveAck()
    }

    "handle Move Left command" in {
      playerCmdAdapter ! ConnectionData(ackProbe.ref, TextMessage(testCommandLEFTJSON))
      engineProbe.receiveMessage() match {
        case Engine.ChangeDirectionReq(playerUpRef, MoveDirection.LEFT) => playerUpRef shouldEqual playerUpdAdapter
        case _ => fail()
      }
      receiveAck()
    }

    "handle Move Right command" in {
      playerCmdAdapter ! ConnectionData(ackProbe.ref, TextMessage(testCommandRIGHTJSON))
      engineProbe.receiveMessage() match {
        case Engine.ChangeDirectionReq(playerUpRef, MoveDirection.RIGHT) => playerUpRef shouldEqual playerUpdAdapter
        case _ => fail()
      }
      receiveAck()
    }

    "handle Move None command" in {
      playerCmdAdapter ! ConnectionData(ackProbe.ref, TextMessage(testCommandNONEJSON))
      engineProbe.receiveMessage() match {
        case Engine.ChangeDirectionCur(playerUpRef) => playerUpRef shouldEqual playerUpdAdapter
        case _ => fail()
      }
      receiveAck()
    }

    "handle Unknown command" in {
      playerCmdAdapter ! ConnectionData(ackProbe.ref, TextMessage(testCommandFakeCmdJSON))
      engineProbe.expectNoMessage()
      receiveAck()
    }

    "handle Unknown Move data command" in {
      playerCmdAdapter ! ConnectionData(ackProbe.ref, TextMessage(testCommandFakeDataCmdJSON))
      engineProbe.expectNoMessage()
      receiveAck()
    }
  }

  private def receiveAck(): Unit =
    ackProbe.receiveMessage() match {
      case ConnectionAck() =>
      case _ => fail()
    }
}
