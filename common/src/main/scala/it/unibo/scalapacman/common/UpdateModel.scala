package it.unibo.scalapacman.common

import it.unibo.scalapacman.lib.math.Point2D


case class GameEntity(id:GameCharacterHolder, pos:Point2D, isDead:Boolean, dir:DirectionHolder)
case class GameState(ghostInFear:Boolean, pacmanEmpowered:Boolean)
case class Pellet(pelletType:DotHolder, pos:Point2D)
case class Item(id:FruitHolder, pos:Point2D)

case class UpdateModel(
                        gameEntities: List[GameEntity],
                        points: Int,
                        state: GameState,
                        pellets: List[Pellet],
                        fruit: Option[Item]
                      )
