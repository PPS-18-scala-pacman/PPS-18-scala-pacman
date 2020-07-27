package it.unibo.scalapacman.common

import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.GameState


case class GameEntity(id:GameCharacterHolder, pos:Point2D, isDead:Boolean, dir:DirectionHolder)
case class Pellet(pelletType:DotHolder, pos:Point2D)
case class Item(id:FruitHolder, pos:Point2D)

case class UpdateModel(
                        gameEntities: List[GameEntity],
                        state: GameState,
                        pellets: List[Pellet],
                        fruit: Option[Item]
                      )
