package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import it.unibo.scalapacman.server.core.Engine.UpdateCommand
import it.unibo.scalapacman.server.model.MoveDirection
import org.scalatest.wordspec.AnyWordSpecLike

class PlayerCommandTest  extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  private var playerCmdAdapter: ActorRef[Message] = _
  private var playerUpdAdapter: ActorRef[UpdateCommand] = _
  private var engineProbe: TestProbe[Engine.EngineCommand] = _

  private var testCommandPauseJSON: String = _
  private var testCommandUPJSON: String = _
  private var testCommandDOWNJSON: String = _
  private var testCommandLEFTJSON: String = _
  private var testCommandRIGHTJSON: String = _
  private var testCommandNONEJSON: String = _
  private var testCommandFakeCmdJSON: String = _
  private var testCommandFakeDataCmdJSON: String = _

  override def beforeAll(): Unit = {
    // creazione e registrazione attore player
    engineProbe = createTestProbe[Engine.EngineCommand]()
    val fakeGameId = "fakeCreateGameId"
    val playerActor = spawn(Player(fakeGameId, engineProbe.ref))
    val regReqSender = createTestProbe[Player.PlayerRegistration]()
    val clientProbe = createTestProbe[Message]()

    engineProbe.receiveMessage() match {
      case Engine.RegisterPlayer(updateRef) => playerUpdAdapter = updateRef
      case _ => fail()
    }
    playerActor ! Player.RegisterUser(regReqSender.ref, clientProbe.ref)
    regReqSender.receiveMessage() match {
      case Player.RegistrationAccepted(ref) => playerCmdAdapter = ref
      case _ => fail()
    }

    //mockup comandi
    testCommandPauseJSON = "{\"id\":{\"commandType\":\"PAUSE\"},\"data\":null}"
    testCommandUPJSON = "{\"id\":{\"commandType\":\"MOVE\"},\"data\":\"{\\\"moveCommandType\\\":\\\"UP\\\"}\"}"
    testCommandDOWNJSON = "{\"id\":{\"commandType\":\"MOVE\"},\"data\":\"{\\\"moveCommandType\\\":\\\"DOWN\\\"}\"}"
    testCommandLEFTJSON = "{\"id\":{\"commandType\":\"MOVE\"},\"data\":\"{\\\"moveCommandType\\\":\\\"LEFT\\\"}\"}"
    testCommandRIGHTJSON = "{\"id\":{\"commandType\":\"MOVE\"},\"data\":\"{\\\"moveCommandType\\\":\\\"RIGHT\\\"}\"}"
    testCommandNONEJSON = "{\"id\":{\"commandType\":\"MOVE\"},\"data\":\"{\\\"moveCommandType\\\":\\\"NONE\\\"}\"}"
    testCommandFakeCmdJSON = "{\"id\":{\"commandType\":\"FAKE\"},\"data\":null}"
    testCommandFakeDataCmdJSON = "{\"id\":{\"commandType\":\"MOVE\"},\"data\":null}"
  }

  "A Player actor" must {

    "handle Pause command" in {
      playerCmdAdapter ! TextMessage(testCommandPauseJSON)
      engineProbe.receiveMessage() match {
        case Engine.SwitchGameState() =>
        case _ => fail()
      }
    }

    "handle Move Up command" in {
      playerCmdAdapter ! TextMessage(testCommandUPJSON)
      engineProbe.receiveMessage() match {
        case Engine.ChangeDirectionReq(playerUpRef, MoveDirection.UP) => playerUpRef shouldEqual playerUpdAdapter
        case _ => fail()
      }
    }

    "handle Move Down command" in {
      playerCmdAdapter ! TextMessage(testCommandDOWNJSON)
      engineProbe.receiveMessage() match {
        case Engine.ChangeDirectionReq(playerUpRef, MoveDirection.DOWN) => playerUpRef shouldEqual playerUpdAdapter
        case _ => fail()
      }
    }

    "handle Move Left command" in {
      playerCmdAdapter ! TextMessage(testCommandLEFTJSON)
      engineProbe.receiveMessage() match {
        case Engine.ChangeDirectionReq(playerUpRef, MoveDirection.LEFT) => playerUpRef shouldEqual playerUpdAdapter
        case _ => fail()
      }
    }

    "handle Move Right command" in {
      playerCmdAdapter ! TextMessage(testCommandRIGHTJSON)
      engineProbe.receiveMessage() match {
        case Engine.ChangeDirectionReq(playerUpRef, MoveDirection.RIGHT) => playerUpRef shouldEqual playerUpdAdapter
        case _ => fail()
      }
    }

    "handle Move None command" in {
      playerCmdAdapter ! TextMessage(testCommandNONEJSON)
      engineProbe.receiveMessage() match {
        case Engine.ChangeDirectionCur(playerUpRef) => playerUpRef shouldEqual playerUpdAdapter
        case _ => fail()
      }
    }

    "handle Unknown command" in {
      playerCmdAdapter ! TextMessage(testCommandFakeCmdJSON)
      engineProbe.expectNoMessage()
    }

    "handle Unknown Move data command" in {
      playerCmdAdapter ! TextMessage(testCommandFakeDataCmdJSON)
      engineProbe.expectNoMessage()
    }
  }
}
