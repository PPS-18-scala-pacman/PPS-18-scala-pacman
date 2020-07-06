package it.unibo.scalapacman.lib.model

sealed trait Dot extends Eatable

abstract class DotAbstract(val points: Int) extends Dot

object Dot {

  // scalastyle:off magic.number

  case object SMALL_DOT extends DotAbstract(10)

  case object ENERGIZER_DOT extends DotAbstract(50)

  // scalastyle:on magic.number
}
