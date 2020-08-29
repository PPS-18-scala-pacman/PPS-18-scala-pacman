package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import it.unibo.scalapacman.common.GameCharacter
import it.unibo.scalapacman.common.GameCharacter.{CLYDE, GameCharacter, INKY, PACMAN}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.GhostType
import it.unibo.scalapacman.server.config.TestSettings.{awaitLowerBound, awaitUpperBound, waitTime}
import it.unibo.scalapacman.server.core.Engine.ChangeDirectionReq
import org.scalatest.wordspec.AnyWordSpecLike

class EngineCommandTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val fakeGameId = "fakeCreateGameId"

  private var engineActor: ActorRef[Engine.EngineCommand] = _
  private var watcherPlayerProbe: TestProbe[Engine.UpdateCommand] = _
  private var watcherMap: Map[GameCharacter, TestProbe[Engine.UpdateCommand]] = _

  override def beforeAll(): Unit = {
    engineActor = spawn(Engine(fakeGameId, 1))
    watcherPlayerProbe = createTestProbe[Engine.UpdateCommand]()

    engineActor ! Engine.RegisterPlayer(watcherPlayerProbe.ref)
    val ghostMap = GhostType.values.map( gt => {
      val curProbe = createTestProbe[Engine.UpdateCommand]()
      engineActor ! Engine.RegisterGhost(curProbe.ref, gt)
      GameCharacter.ghostTypeToGameCharacter(gt) -> curProbe
    }).toMap

    watcherMap = ghostMap + (PACMAN -> watcherPlayerProbe)
  }

  "An Engine actor" must {
    "stops after pause command" in {

      engineActor ! Engine.Pause()

      watcherPlayerProbe.expectNoMessage(waitTime)
    }

    "resume game after resume command" in {

      engineActor ! Engine.Pause()
      //attendo che il gioco sia messo in pausa
      Thread.sleep(waitTime.toMillis)

      engineActor ! Engine.Resume()
      watcherPlayerProbe.receiveMessage(waitTime) match {
        case Engine.UpdateMsg(_) =>
        case _ => fail()
      }
    }

    "change direction when requested for all game characters" in {
      //rimuovo dal controllo inky e clyde perchè a inizio partita sono morti
      GameCharacter.values.filter(gameChar => gameChar != INKY && gameChar != CLYDE).foreach(gameChar => {
        val probe = watcherMap(gameChar)
        var newDirection: Option[Direction] = None

        probe.receiveMessage(waitTime) match {
          case Engine.UpdateMsg(model) =>
            val charDTO = model.gameEntities.find(_.gameCharacterHolder.gameChar == gameChar)
            assert(charDTO.isDefined)
            newDirection = Some(charDTO.get.dir.direction.reverse)
            engineActor ! ChangeDirectionReq(probe.ref, newDirection.get)
          case _ => fail()
        }

        TestProbe().awaitAssert({
          probe.receiveMessage() match {
            case Engine.UpdateMsg(model) =>
              val charDTO = model.gameEntities.find(_.gameCharacterHolder.gameChar == gameChar)
              assert(charDTO.isDefined)
              val curDirection = Some(charDTO.get.dir.direction)
              curDirection shouldEqual newDirection
            case _ => fail()
          }
        }, awaitUpperBound, awaitLowerBound)
      })
    }
  }
}
