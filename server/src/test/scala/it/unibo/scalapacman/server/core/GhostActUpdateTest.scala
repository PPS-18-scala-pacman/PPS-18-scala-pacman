package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.{ActorRef, MailboxSelector}
import it.unibo.scalapacman.common.{DirectionHolder, GameCharacter, GameCharacterHolder, GameEntityDTO, UpdateModelDTO}
import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.{Direction, GameState, GhostType}
import it.unibo.scalapacman.server.util.ConfLoader
import org.scalatest.wordspec.AnyWordSpecLike

class GhostActUpdateTest  extends ScalaTestWithActorTestKit(ConfLoader.config) with AnyWordSpecLike {

  private var ghostActor: ActorRef[Engine.UpdateCommand] = _
  private var engineProbe: TestProbe[Engine.EngineCommand] = _

  private var testModel: UpdateModelDTO = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    val ghostTestType = GhostType.BLINKY

    // scalastyle:off magic.number
    val gameEntities:Set[GameEntityDTO] = Set(
      GameEntityDTO(GameCharacterHolder(GameCharacter.PACMAN), Point2D(1,2), 1, isDead=false, DirectionHolder(Direction.NORTH)),
      GameEntityDTO(GameCharacterHolder(ghostTestType), Point2D(3,4), 1, isDead=false, DirectionHolder(Direction.NORTH)))
    // scalastyle:on magic.number

    testModel = UpdateModelDTO(gameEntities, GameState(score = 2), Set(), None)

    engineProbe = createTestProbe[Engine.EngineCommand]()
    val fakeGameId = "fakeCreateGameId"

    val props = MailboxSelector.fromConfig("server-app.ghost-mailbox")
    ghostActor = spawn(GhostAct(fakeGameId, engineProbe.ref, ghostTestType), props)
    engineProbe.receiveMessage()
  }

  "A Ghost actor" must {

    "send ChangeDirection command to Engine" when {
      "needed" in {

        ghostActor ! Engine.UpdateMsg(testModel)

        engineProbe.receiveMessage() match {
          case Engine.ChangeDirectionReq(ghostAct, _) => ghostAct shouldEqual ghostActor
          case _ => fail()
        }
      }
    }
  }
}
