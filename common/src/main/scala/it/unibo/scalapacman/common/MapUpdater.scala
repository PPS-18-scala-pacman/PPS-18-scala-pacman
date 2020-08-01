package it.unibo.scalapacman.common

import it.unibo.scalapacman.lib.model.{Dot, Fruit, Map}
import it.unibo.scalapacman.lib.engine.GameHelpers.MapHelper

object MapUpdater {

  def update(map: Map, pellets: Set[Pellet], fruit: Option[Item]): Map =
    Some(map) map (updateFromPellets(_, pellets)) map (updateFromFruit(_, fruit)) getOrElse map

  private def updateFromPellets(map: Map, pellets: Set[Pellet]): Map =
    map.eatablesToSeq[Dot.Val]
      .filter(dotInfo => !pellets.exists(_.pos == dotInfo._1))
      .foldLeft(map)((map, dotInfo) => map.empty(dotInfo._1))

  private def updateFromFruit(map: Map, fruit: Option[Item]): Map = fruit match {
    case Some(fruitItem) => map.putEatable(fruitItem.pos, Some(fruitItem.id.fruit))
    case None =>  map.eatablesToSeq[Fruit.Val].foldLeft(map)((map, fruitInfo) => map.empty(fruitInfo._1))
  }
}
