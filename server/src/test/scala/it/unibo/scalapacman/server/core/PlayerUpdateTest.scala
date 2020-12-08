package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import it.unibo.scalapacman.common.{DirectionHolder, DotHolder, FruitHolder, GameCharacter, GameCharacterHolder}
import it.unibo.scalapacman.common.{DotDTO, FruitDTO, GameEntityDTO, UpdateModelDTO}
import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit, GameState}
import it.unibo.scalapacman.server.communication.ConnectionProtocol.{Ack, ConnectionAck, ConnectionInit}
import it.unibo.scalapacman.server.core.Game.GameCommand
import org.scalatest.wordspec.AnyWordSpecLike

class PlayerUpdateTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  private val fakeGameId = "fakeCreateGameId"
  private val playerId = "playerId"

  var testModel: UpdateModelDTO = _
  var testModelJSON: String = _

  override def beforeAll(): Unit = {
    // scalastyle:off magic.number
    val gameEntities:Set[GameEntityDTO] = Set(
      GameEntityDTO(playerId, GameCharacterHolder(GameCharacter.PACMAN), Point2D(1,2), 1, isDead=false, DirectionHolder(Direction.NORTH)),
      GameEntityDTO("2", GameCharacterHolder(GameCharacter.BLINKY), Point2D(3,4), 1, isDead=false, DirectionHolder(Direction.NORTH)),
      GameEntityDTO("3", GameCharacterHolder(GameCharacter.CLYDE),  Point2D(5,6), 1, isDead=false, DirectionHolder(Direction.NORTH)))

    val dots: Set[DotDTO] = Set(
      DotDTO(DotHolder(Dot.SMALL_DOT), (5, 6)),
      DotDTO(DotHolder(Dot.SMALL_DOT), (6, 6)),
      DotDTO(DotHolder(Dot.SMALL_DOT), (7, 6)),
      DotDTO(DotHolder(Dot.SMALL_DOT), (8, 6)))

    val fruit = Some(FruitDTO(FruitHolder(Fruit.APPLE), (9, 9)))
    // scalastyle:on magic.number

    testModel = UpdateModelDTO(gameEntities, GameState(score = 2), dots, fruit)

    testModelJSON = "{\"gameEntities\":[{\"id\":\"" + playerId + "\",\"gameCharacterHolder\":{\"gameChar\":\"PACMAN\"},\"position\":" +
      "{\"x\":1.0,\"y\":2.0},\"speed\":1.0,\"isDead\":false,\"dir\":{\"direction\":\"NORTH\"}},{\"id\":\"2\",\"gameCharacterHolder\":{\"gameChar\":\"" +
      "BLINKY\"},\"position\":{\"x\":3.0,\"y\":4.0},\"speed\":1.0,\"isDead\":false,\"dir\":{\"direction\":\"NORTH\"}},{\"id\":\"3\",\"" +
      "gameCharacterHolder\":{\"gameChar\":\"CLYDE\"},\"position\":{\"x\":5.0,\"y\":6.0},\"speed\":1.0,\"isDead\":false,\"" +
      "dir\":{\"direction\":\"NORTH\"}}],\"state\":{\"score\":2,\"ghostInFear\":false,\"pacmanEmpowered\":false,\"" +
      "levelStateHolder\":{\"levelState\":\"ONGOING\"}},\"dots\":[{\"dotHolder\":{\"dot\":\"SMALL_DOT\"},\"pos\":[5,6]},{\"" +
      "dotHolder\":{\"dot\":\"SMALL_DOT\"},\"pos\":[6,6]},{\"dotHolder\":{\"dot\":\"SMALL_DOT\"},\"pos\":[7,6]},{\"" +
      "dotHolder\":{\"dot\":\"SMALL_DOT\"},\"pos\":[8,6]}],\"fruit\":{\"fruitHolder\":{\"fruit\":\"APPLE\"},\"pos\":[9,9]},\"paused\":false}"
  }

  "A Player actor" must {

    "handle updateModel message" in {
      val engineProbe = createTestProbe[Engine.EngineCommand]()
      val playerActor = spawn(PlayerAct(fakeGameId, engineProbe.ref, createTestProbe[GameCommand]().ref))
      val regReqSender = createTestProbe[PlayerAct.PlayerRegistration]()
      val clientProbe = createTestProbe[Message]()
      val ackProbe = createTestProbe[Ack]()

      playerActor ! PlayerAct.RegisterUser(regReqSender.ref, clientProbe.ref, playerId)
      regReqSender.receiveMessage() match {
        case PlayerAct.RegistrationAccepted(ref) =>
          ref! ConnectionInit(ackProbe.ref)
          ackProbe.expectMessageType[ConnectionAck]
        case _ => fail()
      }

      engineProbe.receiveMessage() match {
        case Engine.RegisterWatcher(updateRef) => updateRef ! Engine.UpdateMsg(testModel)
        case _ => fail()
      }

      clientProbe.expectMessage(TextMessage.Strict(testModelJSON))
    }
  }
}
