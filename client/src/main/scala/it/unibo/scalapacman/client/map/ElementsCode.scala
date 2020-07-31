package it.unibo.scalapacman.client.map

import it.unibo.scalapacman.lib.model.Fruit

object ElementsCode {
  /* Pacman */
  val PACMAN_RIGHT_CODE = "ᗧ"
  val PACMAN_LEFT_CODE = "ᗤ"
  val PACMAN_UP_CODE = "ᗢ"
  val PACMAN_DOWN_CODE = "ᗣ"
  /* Spazio vuoto */
//  val EMPTY_SPACE_CODE   = "  " // 2 non breaking spaces
  val EMPTY_SPACE_CODE = "　" // \u3000
  /* Dot ed Energized Dot */
  val DOT_CODE = "・"
  val ENERGIZER_DOT_CODE = "❂"
  /* Muri */
  val WALL_CODE = "██" // 2 \u2558
  /* Fantasma */
  val GHOST_CODE = "⍾"

  val FRUIT_CODES: Seq[String] = Seq("①", "⓶", "⓷", "⓸", "⓹", "⓺", "⓻", "⓼")

  // scalastyle:off
  val FRUIT_CODES_MAP: Map[Fruit.Value, String] = Map(
    Fruit.CHERRIES -> FRUIT_CODES.head,
    Fruit.STRAWBERRY -> FRUIT_CODES(1),
    Fruit.PEACH -> FRUIT_CODES(2),
    Fruit.APPLE -> FRUIT_CODES(3),
    Fruit.GRAPES -> FRUIT_CODES(4),
    Fruit.GALAXIAN -> FRUIT_CODES(5),
    Fruit.BELL -> FRUIT_CODES(6),
    Fruit.KEY -> FRUIT_CODES(7)
  )
  // scalastyle:on

  def matchPacman(code: String): Boolean = code match {
    case PACMAN_UP_CODE | PACMAN_DOWN_CODE | PACMAN_RIGHT_CODE | PACMAN_LEFT_CODE => true
    case _ => false
  }

  def matchFruit(code: String): Boolean = FRUIT_CODES.contains(code)
}
