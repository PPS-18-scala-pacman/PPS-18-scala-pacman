package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import it.unibo.scalapacman.common.{DirectionHolder, DotHolder, FruitHolder, GameCharacter, GameCharacterHolder}
import it.unibo.scalapacman.common.{GameEntityDTO, FruitDTO, DotDTO, UpdateModelDTO}
import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit, GameState}
import org.scalatest.wordspec.AnyWordSpecLike

class PlayerUpdateTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  private val fakeGameId = "fakeCreateGameId"

  var testModel: UpdateModelDTO = _
  var testModelJSON: String = _

  override def beforeAll(): Unit = {
    // scalastyle:off magic.number
    val gameEntities:Set[GameEntityDTO] = Set(
      GameEntityDTO(GameCharacterHolder(GameCharacter.PACMAN), Point2D(1,2), 1, isDead=false, DirectionHolder(Direction.NORTH)),
      GameEntityDTO(GameCharacterHolder(GameCharacter.BLINKY), Point2D(3,4), 1, isDead=false, DirectionHolder(Direction.NORTH)),
      GameEntityDTO(GameCharacterHolder(GameCharacter.CLYDE),  Point2D(5,6), 1, isDead=false, DirectionHolder(Direction.NORTH)))

    val dots: Set[DotDTO] = Set(
      DotDTO(DotHolder(Dot.SMALL_DOT), (5, 6)),
      DotDTO(DotHolder(Dot.SMALL_DOT), (6, 6)),
      DotDTO(DotHolder(Dot.SMALL_DOT), (7, 6)),
      DotDTO(DotHolder(Dot.SMALL_DOT), (8, 6)))

    val fruit = Some(FruitDTO(FruitHolder(Fruit.APPLE), (9, 9)))
    // scalastyle:on magic.number

    testModel = UpdateModelDTO(gameEntities, GameState(score = 2), dots, fruit)

    testModelJSON = "{\"gameEntities\":[{\"gameCharacterHolder\":{\"gameChar\":\"PACMAN\"},\"position\":{\"x\":1.0,\"y\"" +
      ":2.0},\"speed\":1.0,\"isDead\":false,\"dir\":{\"direction\":\"NORTH\"}},{\"gameCharacterHolder\":{\"gameChar\":\"" +
      "BLINKY\"},\"position\":{\"x\":3.0,\"y\":4.0},\"speed\":1.0,\"isDead\":false,\"dir\":{\"direction\":\"NORTH\"}},{\"" +
      "gameCharacterHolder\":{\"gameChar\":\"CLYDE\"},\"position\":{\"x\":5.0,\"y\":6.0},\"speed\":1.0,\"isDead\":false,\"" +
      "dir\":{\"direction\":\"NORTH\"}}],\"state\":{\"score\":2,\"ghostInFear\":false,\"pacmanEmpowered\":false,\"" +
      "levelStateHolder\":{\"levelState\":\"ONGOING\"}},\"dots\":[{\"dotHolder\":{\"dot\":\"SMALL_DOT\"},\"pos\":[5,6]},{\"" +
      "dotHolder\":{\"dot\":\"SMALL_DOT\"},\"pos\":[6,6]},{\"dotHolder\":{\"dot\":\"SMALL_DOT\"},\"pos\":[7,6]},{\"" +
      "dotHolder\":{\"dot\":\"SMALL_DOT\"},\"pos\":[8,6]}],\"fruit\":{\"fruitHolder\":{\"fruit\":\"APPLE\"},\"pos\":[9,9]}}"
  }

  "A Player actor" must {

    "handle updateModel message" in {
      val engineProbe = createTestProbe[Engine.EngineCommand]()
      val playerActor = spawn(Player(fakeGameId, engineProbe.ref))
      val fooReqSender = createTestProbe[Player.PlayerRegistration]()
      val clientProbe = createTestProbe[Message]()

      playerActor ! Player.RegisterUser(fooReqSender.ref, clientProbe.ref)
      fooReqSender.receiveMessage()

      engineProbe.receiveMessage() match {
        case Engine.RegisterPlayer(updateRef) => updateRef ! Engine.UpdateMsg(testModel)
        case _ => fail()
      }

      clientProbe.receiveMessage() match {
        case TextMessage.Strict(msg) => msg shouldEqual testModelJSON
        case _ => fail()
      }
    }
  }
}
