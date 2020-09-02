package it.unibo.scalapacman.common

import it.unibo.scalapacman.lib.model.{Dot, Fruit, Map}
import it.unibo.scalapacman.lib.engine.GameHelpers.MapHelper

object MapUpdater {

  def update(map: Map, dots: Set[DotDTO], fruit: Option[FruitDTO]): Map =
    Some(map) map (updateFromDots(_, dots)) map (updateFromFruit(_, fruit)) getOrElse map

  private def updateFromDots(map: Map, dots: Set[DotDTO]): Map =
    map.dots
      .filter(dotInfo => !dots.exists(_.pos == dotInfo._1))
      .foldLeft(map)((map, dotInfo) => map.empty(dotInfo._1))

  private def updateFromFruit(map: Map, fruit: Option[FruitDTO]): Map = fruit match {
    case Some(fruitItem) => map.putEatable(fruitItem.pos, Some(fruitItem.fruitHolder.fruit))
    case None =>  map.fruit.foldLeft(map)((map, fruitInfo) => map.empty(fruitInfo._1))
  }
}
