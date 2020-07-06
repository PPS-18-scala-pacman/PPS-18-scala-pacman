package it.unibo.scalapacman.lib.model

sealed trait Fruit extends Eatable

abstract class FruitAbstract(val points: Int) extends Fruit

object Fruit {

  // scalastyle:off magic.number

  case object CHERRIES extends FruitAbstract(100)

  case object STRAWBERRY extends FruitAbstract(300)

  case object PEACH extends FruitAbstract(500)

  case object APPLE extends FruitAbstract(700)

  case object GRAPES extends FruitAbstract(1000)

  case object GALAXIAN extends FruitAbstract(2000)

  case object BELL extends FruitAbstract(3000)

  case object KEY extends FruitAbstract(5000)

  // scalastyle:on magic.number
}
