package it.unibo.scalapacman.common

import java.io.StringWriter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit, GameState}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UpdateModelDTOParsingTest extends AnyWordSpec with BeforeAndAfterAll with Matchers {

  var testModel: UpdateModelDTO = _
  var testModelJSON: String = _
  var mapper:ObjectMapper = _

  override def beforeAll(): Unit = {

    mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    // scalastyle:off magic.number
    val gameEntities:Set[GameEntityDTO] = Set(
      GameEntityDTO("1", GameCharacterHolder(GameCharacter.PACMAN), Point2D(1,2), 1, isDead=false, DirectionHolder(Direction.NORTH)),
      GameEntityDTO("2", GameCharacterHolder(GameCharacter.BLINKY), Point2D(3,4), 1, isDead=false, DirectionHolder(Direction.NORTH)),
      GameEntityDTO("3", GameCharacterHolder(GameCharacter.INKY), Point2D(5,6), 1, isDead=false, DirectionHolder(Direction.NORTH)))

    val dots: Set[DotDTO] = Set(
      DotDTO(DotHolder(Dot.SMALL_DOT), (5,6)),
      DotDTO(DotHolder(Dot.SMALL_DOT), (6,6)),
      DotDTO(DotHolder(Dot.SMALL_DOT), (7,6)),
      DotDTO(DotHolder(Dot.SMALL_DOT), (8,6)))

    val fruit = Some(FruitDTO(FruitHolder(Fruit.APPLE), (9,9)))
    // scalastyle:on magic.number

    testModel = UpdateModelDTO(gameEntities, GameState(score = 2), dots, fruit)

    testModelJSON = "{\"gameEntities\":[{\"id\":\"1\",\"gameCharacterHolder\":{\"gameChar\":\"PACMAN\"},\"position\":{\"x\":1.0,\"y\"" +
      ":2.0},\"speed\":1.0,\"isDead\":false,\"dir\":{\"direction\":\"NORTH\"}},{\"id\":\"2\",\"gameCharacterHolder\":{\"gameChar\":\"" +
      "BLINKY\"},\"position\":{\"x\":3.0,\"y\":4.0},\"speed\":1.0,\"isDead\":false,\"dir\":{\"direction\":\"NORTH\"}},{\"id\":\"3\",\"" +
      "gameCharacterHolder\":{\"gameChar\":\"INKY\"},\"position\":{\"x\":5.0,\"y\":6.0},\"speed\":1.0,\"isDead\":false,\"" +
      "dir\":{\"direction\":\"NORTH\"}}],\"state\":{\"score\":2,\"ghostInFear\":false,\"pacmanEmpowered\":false,\"" +
      "levelStateHolder\":{\"levelState\":\"ONGOING\"}},\"dots\":[{\"dotHolder\":{\"dot\":\"SMALL_DOT\"},\"pos\":[5,6]}," +
      "{\"dotHolder\":{\"dot\":\"SMALL_DOT\"},\"pos\":[6,6]},{\"dotHolder\":{\"dot\":\"SMALL_DOT\"},\"pos\":[7,6]},{\"" +
      "dotHolder\":{\"dot\":\"SMALL_DOT\"},\"pos\":[8,6]}],\"fruit\":{\"fruitHolder\":{\"fruit\":\"APPLE\"},\"pos\":[9,9]}}"
  }

  "An UpdateModel" must {
    "be serializeable in a valid json string" in {
      val out = new StringWriter
      mapper.writeValue(out, testModel)

      out.toString shouldEqual testModelJSON
    }

    "be deserializeable in a valid object" in {
      val model = mapper.readValue(testModelJSON, classOf[UpdateModelDTO])

      model shouldEqual testModel
    }
  }
}
