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
  def mirrorList[A](list: List[A]): List[A] = list ::: list.reverse

  // scalastyle:off structural.type
  def using[A <: {def close(): Unit}, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      resource.close()
    }
  // scalastyle:on structural.type

  def readFile(filename: URL): String =
    using(Source.fromURL(filename))(_.getLines.mkString("\n"))

  def iteratorToList[A](iterator: java.util.Iterator[A]): List[A] = {
    val buffer = mutable.Buffer[A]()
    iterator.forEachRemaining(buffer.append(_))
    buffer.toList
  }
}
