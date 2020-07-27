package it.unibo.scalapacman.lib.model

object Dot extends Enumeration {

  protected case class Val(points: Int) extends super.Val with Eatable
  import scala.language.implicitConversions
  implicit def valueToPlanetVal(x: Value): Val = x.asInstanceOf[Val]

  // scalastyle:off magic.number
  val SMALL_DOT       : Val = Val(10)
  val ENERGIZER_DOT   : Val = Val(50)
  // scalastyle:on magic.number
}
