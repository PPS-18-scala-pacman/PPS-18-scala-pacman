package it.unibo.scalapacman.lib.model

object PlayerType extends Enumeration {
  case class PlayerType(index: Int) extends super.Val
  implicit def valueToPlayerTypeVal(x: Value): PlayerType = x.asInstanceOf[PlayerType]

  implicit def indexToPlayerTypeVal(index: Int): PlayerType = index match {
    case 0 => PLAYER_ONE
    case 1 => PLAYER_TWO
    case 2 => PLAYER_THREE
    case 3 => PLAYER_FOUR
  }

  // scalastyle:off magic.number
  val PLAYER_ONE: PlayerType = PlayerType(0)
  val PLAYER_TWO: PlayerType = PlayerType(1)
  val PLAYER_THREE: PlayerType = PlayerType(2)
  val PLAYER_FOUR: PlayerType = PlayerType(3)
  // scalastyle:on magic.number
}
