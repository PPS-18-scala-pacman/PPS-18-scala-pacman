package it.unibo.scalapacman.lib

import java.net.URL

import scala.io.Source
import scala.language.reflectiveCalls

object Utility {
  def mirrorList[A](list: List[A]): List[A] = list ::: list.reverse

  def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      resource.close()
    }

  def readFile(filename: URL): String =
    using(Source.fromURL(filename))(_.getLines.mkString)

}
