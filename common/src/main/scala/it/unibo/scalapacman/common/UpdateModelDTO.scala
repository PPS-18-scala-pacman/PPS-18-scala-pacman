package it.unibo.scalapacman.common

import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.GhostType.{BLINKY, CLYDE, INKY, PINKY}
import it.unibo.scalapacman.lib.model.Map.MapIndexes
import it.unibo.scalapacman.lib.model.{Dot, Fruit, GameState, Ghost, Pacman}


case class GameEntityDTO(gameCharacterHolder: GameCharacterHolder, position: Point2D, speed: Double, isDead: Boolean, dir: DirectionHolder) {
  def toGhost: Option[Ghost] = gameCharacterHolder.gameChar match {
    case GameCharacter.INKY   => Some(Ghost(INKY, position, speed, dir.direction, isDead))
    case GameCharacter.BLINKY => Some(Ghost(BLINKY, position, speed, dir.direction, isDead))
    case GameCharacter.CLYDE  => Some(Ghost(CLYDE, position, speed, dir.direction, isDead))
    case GameCharacter.PINKY  => Some(Ghost(PINKY, position, speed, dir.direction, isDead))
    case _ => None
  }
  def toPacman: Option[Pacman] =
    if(gameCharacterHolder.gameChar == GameCharacter.PACMAN) Some(Pacman(position, speed, dir.direction, isDead)) else None
}

case class DotDTO(dotHolder: DotHolder, pos: MapIndexes)
case class FruitDTO(fruitHolder: FruitHolder, pos: MapIndexes)
case class Pellet(pelletType:DotHolder, pos:MapIndexes)
object Pellet {
  implicit def rawToPellet(raw: (MapIndexes, Dot.Val)):Pellet = Pellet(DotHolder(raw._2), raw._1)
}

case class Item(id:FruitHolder, pos:MapIndexes)
object Item {
  implicit def rawToItem(raw: (MapIndexes, Fruit.Val)):Item = Item(FruitHolder(raw._2), raw._1)
}

case class UpdateModelDTO(
                           gameEntities: Set[GameEntityDTO],
                           state: GameState,
                           dots: Set[DotDTO],
                           fruit: Option[FruitDTO]
                      )
