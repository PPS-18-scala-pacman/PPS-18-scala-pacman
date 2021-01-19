package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.{ActorRef, MailboxSelector}
import it.unibo.scalapacman.common.{DirectionHolder, GameCharacter, GameCharacterHolder, GameEntityDTO, GameStateDTO, LevelStateHolder, UpdateModelDTO}
import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.{Direction, GameState, GhostType, LevelState}
import it.unibo.scalapacman.server.config.ConfLoader
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

class GhostActUpdateTest extends ScalaTestWithActorTestKit(ConfLoader.akkaConf) with AnyWordSpecLike with BeforeAndAfterEach {

  val fakeGameId = "fakeCreateGameId"

  val ghostTestType: GhostType.GhostType = GhostType.BLINKY
  val ghostId = "ghostId"

  private var ghostActor  : ActorRef[Engine.UpdateCommand] = _
  private var engineProbe : TestProbe[Engine.EngineCommand] = _

  private var testUpdateModel     : UpdateModelDTO = _
  private var testDeadGhostModel  : UpdateModelDTO = _
  private var testEndGameModel    : UpdateModelDTO = _

  override def beforeAll(): Unit = {
    super.beforeAll()

    // scalastyle:off magic.number
    val pacman = GameEntityDTO("1", GameCharacterHolder(GameCharacter.PACMAN), Point2D(48,28), 1, isDead=false, DirectionHolder(Direction.WEST))
    val aliveGhost = GameEntityDTO(ghostId, GameCharacterHolder(ghostTestType), Point2D(120,80), 1, isDead=false, DirectionHolder(Direction.NORTH))
    val deadGhost = aliveGhost.copy(isDead = true)

    testUpdateModel = UpdateModelDTO(Set(aliveGhost, pacman), GameState(score = 2), Set(), None)

    testDeadGhostModel = UpdateModelDTO(Set(deadGhost, pacman), GameState(score = 2), Set(), None)

    val gameStateVictory = GameStateDTO(0, ghostInFear=false, pacmanEmpowered=false, LevelStateHolder(LevelState.VICTORY))
    testEndGameModel = UpdateModelDTO(Set(aliveGhost, pacman), gameStateVictory, Set(), None)
    // scalastyle:on magic.number
  }

  override def beforeEach(): Unit = {
    engineProbe = createTestProbe[Engine.EngineCommand]()

    val props = MailboxSelector.fromConfig("ghost-mailbox")
    ghostActor = spawn(GhostAct(fakeGameId, engineProbe.ref, ghostId), props)
    engineProbe.receiveMessage()
  }

  "A Ghost actor" must {

    "send ChangeDirection request command to Engine" when {
      "ghost is alive" in {

        ghostActor ! Engine.UpdateMsg(testUpdateModel)

        engineProbe.receiveMessage() match {
          case Engine.ChangeDirectionReq(`ghostId`, _) =>
          case _ => fail()
        }
      }
    }

    "send ChangeDirection to current command to Engine" when {
      "ghost is dead and desired direction was sent" in {

        ghostActor ! Engine.UpdateMsg(testUpdateModel)

        engineProbe.receiveMessage() match {
          case Engine.ChangeDirectionReq(`ghostId`, _) =>
          case _ => fail()
        }

        ghostActor ! Engine.UpdateMsg(testDeadGhostModel)

        engineProbe.expectMessage(Engine.ChangeDirectionCur(ghostId))
      }
    }

    "not send message" when {
      "ghost is dead" in {

        ghostActor ! Engine.UpdateMsg(testDeadGhostModel)

        engineProbe.expectNoMessage()
      }
    }

    "terminate when game is over" in {

      ghostActor ! Engine.UpdateMsg(testEndGameModel)

      TestProbe().expectTerminated(ghostActor)
    }
  }
}
