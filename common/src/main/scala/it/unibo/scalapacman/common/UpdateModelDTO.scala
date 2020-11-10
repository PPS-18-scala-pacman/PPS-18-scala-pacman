package it.unibo.scalapacman.common

import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import it.unibo.scalapacman.lib.model.GhostType.{BLINKY, CLYDE, INKY, PINKY}
import it.unibo.scalapacman.lib.model.Map.MapIndexes
import it.unibo.scalapacman.lib.model.{Dot, Fruit, GameState}
import it.unibo.scalapacman.lib.model.PlayerType.{PLAYER_ONE, PLAYER_TWO, PLAYER_THREE, PLAYER_FOUR}


case class GameEntityDTO(gameCharacterHolder: GameCharacterHolder, id: Option[String], position: Point2D,
                         speed: Double, isDead: Boolean, dir: DirectionHolder) {
  def toGhost: Option[Ghost] = gameCharacterHolder.gameChar match {
    case GameCharacter.INKY   => Some(Ghost(INKY,   position, speed, dir.direction, isDead))
    case GameCharacter.BLINKY => Some(Ghost(BLINKY, position, speed, dir.direction, isDead))
    case GameCharacter.CLYDE  => Some(Ghost(CLYDE,  position, speed, dir.direction, isDead))
    case GameCharacter.PINKY  => Some(Ghost(PINKY,  position, speed, dir.direction, isDead))
    case _ => None
  }
  def toPacman: Option[Pacman] = gameCharacterHolder.gameChar match {
    case GameCharacter.PLAYER_ONE   => Some(Pacman(position, speed, dir.direction, isDead, PLAYER_ONE))
    case GameCharacter.PLAYER_TWO   => Some(Pacman(position, speed, dir.direction, isDead, PLAYER_TWO))
    case GameCharacter.PLAYER_THREE => Some(Pacman(position, speed, dir.direction, isDead, PLAYER_THREE))
    case GameCharacter.PLAYER_FOUR  => Some(Pacman(position, speed, dir.direction, isDead, PLAYER_FOUR))
    case _ => None
  }
}

case class DotDTO(dotHolder: DotHolder, pos: MapIndexes)
object DotDTO {
  implicit def rawToDotDTO(raw: (MapIndexes, Dot.Dot)): DotDTO = DotDTO(DotHolder(raw._2), raw._1)
}

case class FruitDTO(fruitHolder: FruitHolder, pos: MapIndexes)
object FruitDTO {
  implicit def rawToFruitDTO(raw: (MapIndexes, Fruit.Fruit)): FruitDTO = FruitDTO(FruitHolder(raw._2), raw._1)
}

case class GameStateDTO(score: Int, ghostInFear: Boolean, pacmanEmpowered: Boolean, levelStateHolder: LevelStateHolder)
object GameStateDTO {
  implicit def gameStateToDTO(gs: GameState): GameStateDTO =
    GameStateDTO(gs.score, gs.ghostInFear, gs.pacmanEmpowered, LevelStateHolder(gs.levelState))

  implicit def dtoToGameState(dto: GameStateDTO): GameState =
    GameState(dto.score, dto.ghostInFear, dto.pacmanEmpowered, dto.levelStateHolder.levelState)
}

case class UpdateModelDTO(
                           gameEntities: Set[GameEntityDTO],
                           state: GameStateDTO,
                           dots: Set[DotDTO],
                           fruit: Option[FruitDTO]
                         )
