package it.unibo.scalapacman.lib.model

object Fruit extends Enumeration {

  case class Fruit(points: Int) extends super.Val with Eatable
  implicit def valueToFruitVal(x: Value): Fruit = x.asInstanceOf[Fruit]

  // scalastyle:off magic.number
  val CHERRIES    : Fruit = Fruit(100)
  val STRAWBERRY  : Fruit = Fruit(300)
  val PEACH       : Fruit = Fruit(500)
  val APPLE       : Fruit = Fruit(700)
  val GRAPES      : Fruit = Fruit(1000)
  val GALAXIAN    : Fruit = Fruit(2000)
  val BELL        : Fruit = Fruit(3000)
  val KEY         : Fruit = Fruit(5000)
  // scalastyle:on magic.number
}
