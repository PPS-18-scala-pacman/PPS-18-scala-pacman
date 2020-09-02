package it.unibo.scalapacman.lib.model

object Dot extends Enumeration {

  case class Dot(points: Int) extends super.Val with Eatable
  implicit def valueToDotVal(x: Value): Dot = x.asInstanceOf[Dot]

  // scalastyle:off magic.number
  val SMALL_DOT       : Dot = Dot(10)
  val ENERGIZER_DOT   : Dot = Dot(50)
  // scalastyle:on magic.number
}
