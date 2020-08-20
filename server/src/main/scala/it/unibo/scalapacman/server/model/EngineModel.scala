package it.unibo.scalapacman.server.model

import akka.actor.typed.ActorRef
import it.unibo.scalapacman.common.{DirectionHolder, GameCharacter, GameCharacterHolder, GameEntityDTO}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.Direction.{EAST, NORTH, SOUTH, WEST, NORTHEAST, NORTHWEST, SOUTHWEST, SOUTHEAST}
import it.unibo.scalapacman.lib.model.{Character, Direction, GameState, Ghost, Map, Pacman}
import it.unibo.scalapacman.server.core.Engine.UpdateCommand

import scala.collection.immutable


object MoveDirection extends Enumeration {
  type MoveDirection = Value
  val UP, DOWN, RIGHT, LEFT = Value

  implicit def moveDirectionToDirection(move: MoveDirection): Direction = move match {
    case UP     => NORTH
    case DOWN   => SOUTH
    case LEFT   => WEST
    case RIGHT  => EAST
  }

  implicit def directionToMoveDirection(dir: Direction): MoveDirection = dir match {
    case NORTH | NORTHEAST | NORTHWEST  => UP
    case SOUTH | SOUTHEAST | SOUTHWEST  => DOWN
    case EAST                           => RIGHT
    case WEST                           => LEFT
  }
}

case class RegisteredParticipant(actor: ActorRef[UpdateCommand])

case class StarterModel(
                         blinky: Option[RegisteredParticipant] = None,
                         pinky: Option[RegisteredParticipant] = None,
                         inky: Option[RegisteredParticipant] = None,
                         clyde: Option[RegisteredParticipant] = None,
                         pacman: Option[RegisteredParticipant] = None
                       ) {
  def toSeq: immutable.Seq[RegisteredParticipant] =
    (blinky :: pinky :: inky :: clyde :: pacman :: Nil) filter (_.isDefined) map (_.get)

  def isFull: Boolean = toSeq.size == 5
}

case class GameParticipant(
                            character: Character,
                            actRef: ActorRef[UpdateCommand],
                            desiredDir: Option[Direction] = None
                          )

object GameParticipant {

  implicit def gameParticipantToGameEntity(participant: GameParticipant): GameEntityDTO = participant match {
    case GameParticipant(Ghost(gType, pos, speed, dir, isDead), _, _) =>
      GameEntityDTO(GameCharacterHolder(gType), pos, speed,  isDead, DirectionHolder(dir))
    case GameParticipant(Pacman(pos, speed, dir, isDead), _, _) =>
      GameEntityDTO(GameCharacterHolder(GameCharacter.PACMAN), pos, speed,  isDead, DirectionHolder(dir))
  }
}

case class Players(
                    blinky: GameParticipant,
                    pinky: GameParticipant,
                    inky: GameParticipant,
                    clyde: GameParticipant,
                    pacman: GameParticipant
                  ) {
  def toSet: Set[GameParticipant] = Set(blinky, pinky, inky, clyde, pacman)
}

case class EngineModel(
                        players: Players,
                        map: Map,
                        state: GameState = GameState(score = 0)
                      )
