package it.unibo.scalapacman.common

import java.io.StringWriter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit, GameState}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UpdateModelParsingTest extends AnyWordSpec with BeforeAndAfterAll with Matchers {

  var testModel: UpdateModel = _
  var testModelJSON: String = _
  var mapper:ObjectMapper = _

  override def beforeAll(): Unit = {

    mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    // scalastyle:off magic.number
    val gameEntities:Set[GameEntity] = Set(
      GameEntity(GameCharacterHolder(GameCharacter.PACMAN), Point2D(1,2), 1, isDead=false, DirectionHolder(Direction.NORTH)),
      GameEntity(GameCharacterHolder(GameCharacter.PACMAN), Point2D(3,4), 1, isDead=false, DirectionHolder(Direction.NORTH)),
      GameEntity(GameCharacterHolder(GameCharacter.PACMAN), Point2D(5,6), 1, isDead=false, DirectionHolder(Direction.NORTH)))

    val pellets: Set[Pellet] = Set(
      Pellet(DotHolder(Dot.SMALL_DOT), (5,6)),
      Pellet(DotHolder(Dot.SMALL_DOT), (6,6)),
      Pellet(DotHolder(Dot.SMALL_DOT), (7,6)),
      Pellet(DotHolder(Dot.SMALL_DOT), (8,6)))

    val fruit = Some(Item(FruitHolder(Fruit.APPLE), (9,9)))
    // scalastyle:on magic.number

    testModel = UpdateModel(gameEntities, GameState(score = 2), pellets, fruit)

    testModelJSON = "{\"gameEntities\":[{\"id\":{\"gameChar\":\"PACMAN\"},\"position\":{\"x\":1.0,\"y\":2.0},\"speed" +
      "\":1.0,\"isDead\":false,\"dir\":{\"direction\":\"NORTH\"}},{\"id\":{\"gameChar\":\"PACMAN\"},\"position\":{\"x" +
      "\":3.0,\"y\":4.0},\"speed\":1.0,\"isDead\":false,\"dir\":{\"direction\":\"NORTH\"}},{\"id\":{\"gameChar\":\"" +
      "PACMAN\"},\"position\":{\"x\":5.0,\"y\":6.0},\"speed\":1.0,\"isDead\":false,\"dir\":{\"direction\":\"NORTH\"}}" +
      "],\"state\":{\"score\":2,\"ghostInFear\":false,\"pacmanEmpowered\":false},\"pellets\":[{\"pelletType\":{\"dot" +
      "\":\"SMALL_DOT\"},\"pos\":[5,6]},{\"pelletType\":{\"dot\":\"SMALL_DOT\"},\"pos\":[6,6]},{\"pelletType\":{\"dot" +
      "\":\"SMALL_DOT\"},\"pos\":[7,6]},{\"pelletType\":{\"dot\":\"" +
      "SMALL_DOT\"},\"pos\":[8,6]}],\"fruit\":{\"id\":{\"fruit\":\"APPLE\"},\"pos\":[9,9]}}"
  }

  "An UpdateModel" must {
    "be serializeable in a valid json string" in {
      val out = new StringWriter
      mapper.writeValue(out, testModel)

      out.toString shouldEqual testModelJSON
    }

    "be deserializeable in a valid object" in {
      val model = mapper.readValue(testModelJSON, classOf[UpdateModel])

      model shouldEqual testModel
    }
  }
}
