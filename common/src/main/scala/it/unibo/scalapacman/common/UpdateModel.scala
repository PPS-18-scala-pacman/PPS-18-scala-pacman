package it.unibo.scalapacman.common

import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.GhostType.{BLINKY, CLYDE, INKY, PINKY}
import it.unibo.scalapacman.lib.model.Map.MapIndexes
import it.unibo.scalapacman.lib.model.{GameState, Ghost, Pacman}


case class GameEntity(id:GameCharacterHolder, position:Point2D, speed: Double, isDead:Boolean, dir:DirectionHolder) {
  def toGhost: Option[Ghost] = id.gameChar match {
    case GameCharacter.INKY   => Some(Ghost(INKY, position, speed, dir.direction, isDead))
    case GameCharacter.BLINKY => Some(Ghost(BLINKY, position, speed, dir.direction, isDead))
    case GameCharacter.CLYDE  => Some(Ghost(CLYDE, position, speed, dir.direction, isDead))
    case GameCharacter.PINKY  => Some(Ghost(PINKY, position, speed, dir.direction, isDead))
    case _ => None
  }
  def toPacman: Option[Pacman] =
    if(id.gameChar == GameCharacter.PACMAN) Some(Pacman(position, speed, dir.direction, isDead)) else None
}

case class Pellet(pelletType:DotHolder, pos:MapIndexes)
case class Item(id:FruitHolder, pos:MapIndexes)

case class UpdateModel(
                        gameEntities: Set[GameEntity],
                        state: GameState,
                        pellets: Set[Pellet],
                        fruit: Option[Item]
                      )
