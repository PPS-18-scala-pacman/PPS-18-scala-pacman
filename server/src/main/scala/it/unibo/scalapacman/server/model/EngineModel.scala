package it.unibo.scalapacman.server.model

import it.unibo.scalapacman.common.{DirectionHolder, GameCharacterHolder, GameEntityDTO}
import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.{Character, CharacterType, GameState, GameTimedEvent, Map}
import it.unibo.scalapacman.server.config.Settings

import scala.concurrent.duration.FiniteDuration


case class GameParticipant(nickname: String, character: Character, desiredDir: Option[Direction] = None)

object GameParticipant {

  implicit def gameParticipantToGameEntity(participant: GameParticipant): GameEntityDTO = participant match {
    case GameParticipant(id, Ghost(gType, pos, speed, dir, isDead), _) =>
      GameEntityDTO(id, GameCharacterHolder(gType), pos, speed,  isDead, DirectionHolder(dir))
    case GameParticipant(id, Pacman(pType, pos, speed, dir, isDead), _) =>
      GameEntityDTO(id, GameCharacterHolder(pType), pos, speed,  isDead, DirectionHolder(dir))
    case _ => throw new IllegalArgumentException("Unknown character type")
  }
}

case class GameEntity(nickname: String, charType: CharacterType)

case class GameParameter( players: List[GameEntity], level: Int,
                     gameRefreshRate: FiniteDuration = Settings.gameRefreshRate,
                     pauseRefreshRate: FiniteDuration = Settings.pauseRefreshRate)

case class GameData(participants: List[GameParticipant],
                    map: Map,
                    state: GameState = GameState(score = 0),
                    gameEvents: List[GameTimedEvent[Any]])
