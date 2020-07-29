package it.unibo.scalapacman.server.model

import akka.actor.typed.ActorRef
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.{Character, GameState, Map}
import it.unibo.scalapacman.server.core.Engine.UpdateCommand


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
                       )

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
                  )

case class EngineModel(
                        players: Players,
                        map: Map,
                        state: GameState = GameState(score = 0)
                      )
