package it.unibo.scalapacman.server.model

import akka.actor.typed.ActorRef
import it.unibo.scalapacman.common.{DirectionHolder, GameCharacter, GameCharacterHolder, GameEntity}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.{Character, Direction, GameState, Ghost, Map, Pacman}
import it.unibo.scalapacman.server.core.Engine.UpdateCommand

import scala.collection.immutable


object MoveDirection extends Enumeration {
  type MoveDirection = Value
  val UP, DOWN, RIGHT, LEFT = Value

  implicit def moveDirectionToDirection(dir: MoveDirection): Direction = dir match {
    case MoveDirection.UP     => Direction.NORTH
    case MoveDirection.DOWN   => Direction.SOUTH
    case MoveDirection.LEFT   => Direction.WEST
    case MoveDirection.RIGHT  => Direction.EAST
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

  implicit def gameParticipantToGameEntity(participant: GameParticipant): GameEntity = participant match {
    case GameParticipant(Ghost(gType, pos, speed, dir, isDead), _, _) =>
      GameEntity(GameCharacterHolder(gType), pos, speed,  isDead, DirectionHolder(dir))
    case GameParticipant(Pacman(pos, speed, dir, isDead), _, _) =>
      GameEntity(GameCharacterHolder(GameCharacter.PACMAN), pos, speed,  isDead, DirectionHolder(dir))
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
