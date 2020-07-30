package it.unibo.scalapacman.server.model

import akka.actor.typed.ActorRef
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.{Character, GameState, Map}
import it.unibo.scalapacman.server.core.Engine.UpdateCommand

import scala.collection.immutable


object MoveDirection extends Enumeration {
  type MoveDirection = Value
  val UP, DOWN, RIGHT, LEFT = Value
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

case class Players(
                    blinky: GameParticipant,
                    pinky: GameParticipant,
                    inky: GameParticipant,
                    clyde: GameParticipant,
                    pacman: GameParticipant
                  ) {
  def toSeq: immutable.Seq[GameParticipant] = blinky :: pinky :: inky :: clyde :: pacman :: Nil
}

case class EngineModel(
                        players: Players,
                        map: Map,
                        state: GameState = GameState(score = 0)
                      )
