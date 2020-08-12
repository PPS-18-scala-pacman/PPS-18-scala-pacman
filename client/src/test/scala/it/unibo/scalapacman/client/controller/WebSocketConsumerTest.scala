package it.unibo.scalapacman.client.controller

import it.unibo.scalapacman.common.{DirectionHolder, DotDTO, DotHolder, FruitDTO, FruitHolder, GameCharacter,
  GameCharacterHolder, GameEntityDTO, UpdateModelDTO}
import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit, GameState}
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class WebSocketConsumerTest extends AnyWordSpecLike with MockFactory with BeforeAndAfterAll {

  var _webSocketConsumer: WebSocketConsumer = _
  var testModel: UpdateModelDTO = _
  var testModelJSON: String = _
  var testModelJSONIncorrect: String = _

  override def beforeAll(): Unit = {

    // scalastyle:off magic.number
    val gameEntities:Set[GameEntityDTO] = Set(
      GameEntityDTO(GameCharacterHolder(GameCharacter.PACMAN), Point2D(1,2), 1, isDead=false, DirectionHolder(Direction.NORTH))
    )

    val dots: Set[DotDTO] = Set(DotDTO(DotHolder(Dot.SMALL_DOT), (5,6)))

    val fruit = Some(FruitDTO(FruitHolder(Fruit.APPLE), (9,9)))

    testModel = UpdateModelDTO(gameEntities, GameState(score = 2), dots, fruit)
    // scalastyle:on magic.number

    testModelJSON = "{\"gameEntities\":[{\"gameCharacterHolder\":{\"gameChar\":\"PACMAN\"},\"position\":{\"x\":1.0,\"y\"" +
      ":2.0},\"speed\":1.0,\"isDead\":false,\"dir\":{\"direction\":\"NORTH\"}}],\"state\":{\"score\":2,\"ghostInFear\":false," +
      "\"pacmanEmpowered\":false,\"levelStateHolder\":{\"levelState\":\"ONGOING\"}},\"dots\":[{\"dotHolder\":{\"dot\":\"SMALL_DOT\"}," +
      "\"pos\":[5,6]}],\"fruit\":{\"fruitHolder\":{\"fruit\":\"APPLE\"},\"pos\":[9,9]}}"

    testModelJSONIncorrect = "incorrect"
  }

  "WebSocketConsumer" should {
    "consume an incoming message and notify when correct model" in {
      val mockNotifyUpdateModel = stubFunction[UpdateModelDTO, Unit]

      _webSocketConsumer = new WebSocketConsumer(mockNotifyUpdateModel)

      new Thread(_webSocketConsumer).start()

      // Do tempo al thread di partire
      Thread.sleep(100)// scalastyle:ignore

      _webSocketConsumer.addMessage(testModelJSON)

      // La conversione dal JSON impiega tempo
      Thread.sleep(3000)// scalastyle:ignore

      mockNotifyUpdateModel.verify(testModel).once()

      _webSocketConsumer.terminate()
    }

    "consume an incoming message and not notify when incorrect model" in {
      val mockNotifyUpdateModel = stubFunction[UpdateModelDTO, Unit]

      _webSocketConsumer = new WebSocketConsumer(mockNotifyUpdateModel)

      new Thread(_webSocketConsumer).start()

      // Do tempo al thread di partire
      Thread.sleep(100)// scalastyle:ignore

      _webSocketConsumer.addMessage(testModelJSONIncorrect)

      // La conversione dal JSON impiega tempo
      Thread.sleep(3000)// scalastyle:ignore

      mockNotifyUpdateModel.verify(*).never()

      _webSocketConsumer.terminate()
    }
  }

}
