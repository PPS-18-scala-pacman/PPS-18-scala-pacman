package it.unibo.scalapacman.lib.model

object Fruit extends Enumeration {

  case class Val(points: Int) extends super.Val with Eatable
  implicit def valueToFruitVal(x: Value): Val = x.asInstanceOf[Val]

  // scalastyle:off magic.number
  val CHERRIES    : Val = Val(100)
  val STRAWBERRY  : Val = Val(300)
  val PEACH       : Val = Val(500)
  val APPLE       : Val = Val(700)
  val GRAPES      : Val = Val(1000)
  val GALAXIAN    : Val = Val(2000)
  val BELL        : Val = Val(3000)
  val KEY         : Val = Val(5000)
  // scalastyle:on magic.number
}
