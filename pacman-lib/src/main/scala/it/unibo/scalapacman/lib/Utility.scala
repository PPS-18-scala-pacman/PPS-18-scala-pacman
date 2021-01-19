package it.unibo.scalapacman.lib

import java.net.URL

import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper, MapHelper}
import it.unibo.scalapacman.lib.model.{Character, Direction, Map}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.Map.MapIndexes

import scala.collection.mutable
import scala.io.Source
import scala.language.reflectiveCalls

object Utility {
  /**
   * Data una lista ne appende gli stessi valori in ordine inverso.
   * @param list Lista da invertire
   * @tparam A Tipo della lista
   * @return Lista in input unita alla lista stessa invertita
   */
  def mirrorList[A](list: List[A]): List[A] = list ::: list.reverse

  // scalastyle:off structural.type
  /**
   * Assicura che la risorsa in input venga chiusa, solitamente per liberare risorse,
   * al termine dell'esecuzione della funzione passata
   * @param resource Risorsa che deve essere chiusa
   * @param f Funzione da eseguire sulla risorsa
   * @tparam A Tipo della risorsa
   * @tparam B Tipo dell'output generato dalla funzione
   * @return Il risultato della funzione
   */
  def using[A <: {def close(): Unit}, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      resource.close()
    }
  // scalastyle:on structural.type

  /**
   * Legge un file testuale.
   * @param filename URL del file
   * @return Il testo in forma di stringa
   */
  def readFile(filename: URL): String =
    using(Source.fromURL(filename))(_.getLines.mkString("\n"))

  /**
   * Converte un iterator in lista
   * @param iterator Iteratore da convertire
   * @tparam A Tipo dei valori ritornati dall'iteratore
   * @return Lista contenente tutti i valori iterati
   */
  def iteratorToList[A](iterator: java.util.Iterator[A]): List[A] = {
    val buffer = mutable.Buffer[A]()
    iterator.forEachRemaining(buffer.append(_))
    buffer.toList
  }
}
