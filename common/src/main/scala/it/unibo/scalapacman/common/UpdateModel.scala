package it.unibo.scalapacman.common

import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.GhostType.{BLINKY, CLYDE, INKY, PINKY}
import it.unibo.scalapacman.lib.model.Map.MapIndexes
import it.unibo.scalapacman.lib.model.{Dot, Fruit, GameState, Ghost, Pacman}


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

case class Pellet(pelletType:DotHolder, pos:Point2D)
object Pellet {
  implicit def rawToPellet(raw: (MapIndexes, Dot.Val)):Pellet = Pellet(DotHolder(raw._2), Point2D(raw._1._1, raw._1._2))
}

case class Item(id:FruitHolder, pos:Point2D)
object Item {
  implicit def rawToItem(raw: (MapIndexes, Fruit.Val)):Item = Item(FruitHolder(raw._2), Point2D(raw._1._1, raw._1._2))
}

case class UpdateModel(
                        gameEntities: Set[GameEntity],
                        state: GameState,
                        pellets: Set[Pellet],
                        fruit: Option[Item]
                      )
