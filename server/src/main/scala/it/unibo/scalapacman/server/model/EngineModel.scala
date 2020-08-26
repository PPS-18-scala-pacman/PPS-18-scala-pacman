package it.unibo.scalapacman.server.model

import akka.actor.typed.ActorRef
import it.unibo.scalapacman.common.{DirectionHolder, GameCharacter, GameCharacterHolder, GameEntityDTO}
import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.GhostType.{BLINKY, CLYDE, INKY, PINKY}
import it.unibo.scalapacman.lib.model.{Character, GameState, Map}
import it.unibo.scalapacman.lib.model.Direction.{Direction, EAST, NORTH, NORTHEAST, NORTHWEST, SOUTH, SOUTHEAST, SOUTHWEST, WEST}
import it.unibo.scalapacman.lib.model.{Character, GameState, GameTimedEvent, GhostType, Map}
import it.unibo.scalapacman.server.core.Engine.UpdateCommand


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
    case _ => throw new IllegalArgumentException("Unknown character type")
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

object Players {

  implicit def playerToCharacters(pl: Players): List[Character] =
    pl.pacman.character :: pl.pinky.character :: pl.inky.character :: pl.clyde.character :: pl.blinky.character :: Nil

  implicit def updatePlayers(characters: List[Character])(implicit pl: Players): Players =
    characters.foldRight(pl) {
      case (c@Pacman(_, _, _, _), pl)                   => pl.copy(pacman  = pl.pacman.copy(c))
      case (c@Ghost(BLINKY, _, _, _, _), pl)  => pl.copy(blinky  = pl.blinky.copy(c))
      case (c@Ghost(CLYDE, _, _, _, _), pl)   => pl.copy(clyde   = pl.clyde.copy(c))
      case (c@Ghost(INKY, _, _, _, _), pl)    => pl.copy(inky    = pl.inky.copy(c))
      case (c@Ghost(PINKY, _, _, _, _), pl)   => pl.copy(pinky   = pl.pinky.copy(c))
      case _ => throw new IllegalArgumentException("Unknown character type")
    }
}

case class EngineModel(
                        players: Players,
                        map: Map,
                        state: GameState = GameState(score = 0),
                        gameEvents: List[GameTimedEvent[Any]]
                      )
