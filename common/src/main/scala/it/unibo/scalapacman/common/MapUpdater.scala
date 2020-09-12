package it.unibo.scalapacman.common

import it.unibo.scalapacman.lib.model.{Dot, Fruit, Map}
import it.unibo.scalapacman.lib.engine.GameHelpers.MapHelper

/**
 * Contiene le funzioni di utility per l'aggiornamento di una Map
 */
object MapUpdater {

  /**
   * Restituisce la mappa con le informazioni aggiornate dei pallini (dots) e del frutto
   *
   * @param map   la mappa da dover aggiornare
   * @param dots  la lista di pallini attualmente presenti
   * @param fruit il frutto di questo livello
   * @return      la mappa aggiornata
   */
  def update(map: Map, dots: Set[DotDTO], fruit: Option[FruitDTO]): Map =
    Some(map) map (updateFromDots(_, dots)) map (updateFromFruit(_, fruit)) getOrElse map

  /**
   * Rimuove dalla mappa tutti i pallini che sono stati mangiati
   *
   * @param map   la mappa da dover aggiornare
   * @param dots  la lista di pallini attualmente presenti
   * @return      la mappa aggiornata
   */
  private def updateFromDots(map: Map, dots: Set[DotDTO]): Map =
    map.dots
      .filter(dotInfo => !dots.exists(_.pos == dotInfo._1))
      .foldLeft(map)((map, dotInfo) => map.empty(dotInfo._1))

  /**
   * Rimuove o aggiunge dalla mappa il frutto di questo livello
   *
   * @param map   la mappa da dover aggiornare
   * @param fruit il frutto di questo livello
   * @return      la mappa aggiornata
   */
  private def updateFromFruit(map: Map, fruit: Option[FruitDTO]): Map = fruit match {
    case Some(fruitItem) => map.putEatable(fruitItem.pos, Some(fruitItem.fruitHolder.fruit))
    case None =>  map.fruit.foldLeft(map)((map, fruitInfo) => map.empty(fruitInfo._1))
  }
}
