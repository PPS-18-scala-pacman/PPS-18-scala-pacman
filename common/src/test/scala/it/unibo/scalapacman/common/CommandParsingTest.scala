package it.unibo.scalapacman.common

import java.io.StringWriter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CommandParsingTest extends AnyWordSpec with BeforeAndAfterAll with Matchers {

  var testCommandUP: Command = _
  var testCommandUPData: MoveCommandTypeHolder = _
  var testCommandPause: Command = _
  var testCommandResume: Command = _
  var testCommandUPJSON: String = _
  var testCommandPauseJSON: String = _
  var testCommandResumeJSON: String = _
  var testCommandUPDataJSON: String = _

  var mapper:ObjectMapper = _

  override def beforeAll(): Unit = {

    mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val out = new StringWriter

    testCommandUPData = MoveCommandTypeHolder(MoveCommandType.UP)
    mapper.writeValue(out, testCommandUPData)
    testCommandUP = Command(CommandTypeHolder(CommandType.MOVE), Some(out.toString))

    testCommandPause = Command(CommandTypeHolder(CommandType.PAUSE), None)
    testCommandResume = Command(CommandTypeHolder(CommandType.RESUME), None)

    testCommandUPDataJSON = "{\"moveCommandType\":\"UP\"}"
    testCommandUPJSON = "{\"id\":{\"commandType\":\"MOVE\"},\"data\":\"{\\\"moveCommandType\\\":\\\"UP\\\"}\"}"
    testCommandPauseJSON = "{\"id\":{\"commandType\":\"PAUSE\"},\"data\":null}"
    testCommandResumeJSON = "{\"id\":{\"commandType\":\"RESUME\"},\"data\":null}"
  }

  "A PauseCommand" must {
    "be serializeable in a valid json string" in {
      val out = new StringWriter
      mapper.writeValue(out, testCommandPause)

      out.toString shouldEqual testCommandPauseJSON
    }

    "be deserializeable in a valid object" in {
      val pauseCommand = mapper.readValue(testCommandPauseJSON, classOf[Command])

      pauseCommand shouldEqual testCommandPause
    }
  }

  "A ResumeCommand" must {
    "be serializeable in a valid json string" in {
      val out = new StringWriter
      mapper.writeValue(out, testCommandResume)

      out.toString shouldEqual testCommandResumeJSON
    }

    "be deserializeable in a valid object" in {
      val pauseCommand = mapper.readValue(testCommandResumeJSON, classOf[Command])

      pauseCommand shouldEqual testCommandResume
    }
  }

  "A MoveCommand" must {
    "be serializeable in a valid json message" in {
      var out = new StringWriter

      mapper.writeValue(out, testCommandUPData)
      out.toString shouldEqual testCommandUPDataJSON

      out = new StringWriter
      mapper.writeValue(out, testCommandUP)
      out.toString shouldEqual testCommandUPJSON
    }

    "be deserializeable in a valid command" in {
      val commandUP = mapper.readValue(testCommandUPJSON, classOf[Command])
      commandUP shouldEqual testCommandUP
      assert(commandUP.data.isDefined)

      val commandUPData = mapper.readValue(commandUP.data.get, classOf[MoveCommandTypeHolder])
      commandUPData shouldEqual testCommandUPData
    }
  }
}
