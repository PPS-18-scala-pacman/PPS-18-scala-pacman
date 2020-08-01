package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import it.unibo.scalapacman.common.{DirectionHolder, DotHolder, FruitHolder, GameCharacter, GameCharacterHolder}
import it.unibo.scalapacman.common.{GameEntity, Item, Pellet, UpdateModel}
import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit, GameState}
import org.scalatest.wordspec.AnyWordSpecLike

class PlayerUpdateTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  private val fakeGameId = "fakeCreateGameId"

  private var testModel: UpdateModel = _
  private var testModelJSON: String = _

  override def beforeAll(): Unit = {
    // scalastyle:off magic.number
    val gameEntities:Set[GameEntity] = Set(
      GameEntity(GameCharacterHolder(GameCharacter.PACMAN), Point2D(1,2), 1, isDead=false, DirectionHolder(Direction.NORTH)),
        GameEntity(GameCharacterHolder(GameCharacter.BLINKY), Point2D(3,4), 1, isDead=false, DirectionHolder(Direction.NORTH)),
        GameEntity(GameCharacterHolder(GameCharacter.CLYDE), Point2D(5,6), 1, isDead=false, DirectionHolder(Direction.NORTH)))

    val pellets: Set[Pellet] = Set(
      Pellet(DotHolder(Dot.SMALL_DOT), Point2D(5,6)),
        Pellet(DotHolder(Dot.SMALL_DOT), Point2D(6,6)),
        Pellet(DotHolder(Dot.SMALL_DOT), Point2D(7,6)),
        Pellet(DotHolder(Dot.SMALL_DOT), Point2D(8,6)))

    val fruit = Some(Item(FruitHolder(Fruit.APPLE), Point2D(9,9)))
    // scalastyle:on magic.number

    testModel = UpdateModel(gameEntities, GameState(score = 2), pellets, fruit)

    testModelJSON = "{\"gameEntities\":[{\"id\":{\"gameChar\":\"PACMAN\"},\"position\":{\"x\":1.0,\"y\":2.0},\"speed" +
      "\":1.0,\"isDead\":false,\"dir\":{\"direction\":\"NORTH\"}},{\"id\":{\"gameChar\":\"PACMAN\"},\"position\":{" +
      "\"x\":3.0,\"y\":4.0},\"speed\":1.0,\"isDead\":false,\"dir\":{\"direction\":\"NORTH\"}},{\"id\":{\"gameChar\"" +
      ":\"PACMAN\"},\"position\":{\"x\":5.0,\"y\":6.0},\"speed\":1.0,\"isDead\":false,\"dir\":{\"direction\":\"NORTH" +
      "\"}}],\"state\":{\"score\":2,\"ghostInFear\":false,\"pacmanEmpowered\":false},\"pellets\":[{\"pelletType\":{" +
      "\"dot\":\"SMALL_DOT\"},\"pos\":{\"x\":5.0,\"y\":6.0}},{\"pelletType\":{\"dot\":\"SMALL_DOT\"},\"pos\":{\"x\":" +
      "6.0,\"y\":6.0}},{\"pelletType\":{\"dot\":\"SMALL_DOT\"},\"pos\":{\"x\":7.0,\"y\":6.0}},{\"pelletType\":{\"dot" +
      "\":\"SMALL_DOT\"},\"pos\":{\"x\":8.0,\"y\":6.0}}],\"fruit\":{\"id\":{\"fruit\":\"APPLE\"},\"pos\":{\"x\":9.0," +
      "\"y\":9.0}}}"
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
