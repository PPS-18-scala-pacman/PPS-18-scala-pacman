package it.unibo.scalapacman.common

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class JSONConverterTest extends AnyWordSpec with BeforeAndAfterAll with Matchers {

  var testCommandUPData: MoveCommandTypeHolder = _
  var testCommandUPDataJSON: String = _

  override protected def beforeAll(): Unit = {
    testCommandUPData = MoveCommandTypeHolder(MoveCommandType.UP)
    testCommandUPDataJSON = "{\"moveCommandType\":\"UP\"}"
  }

  "JSONConverter" should {
    "serialize a serializable object" in {
      JSONConverter.toJSON(testCommandUPData) shouldEqual testCommandUPDataJSON
    }

    "deserialize a deserializable object" in {
      JSONConverter.fromJSON[MoveCommandTypeHolder](testCommandUPDataJSON) shouldEqual Some(testCommandUPData)
    }

    "handle conversion error" in {
      val notValidJSONString = "Test"

      JSONConverter.fromJSON[MoveCommandTypeHolder](notValidJSONString) shouldEqual None
    }
  }
}
