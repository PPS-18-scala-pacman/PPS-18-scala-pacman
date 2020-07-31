package it.unibo.scalapacman.client.map

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
  val ENERGIZED_DOT_CODE = "❂"
  /* Muri */
  val WALL_CODE = "██" // 2 \u2558
  /* Fantasma */
  val GHOST_CODE = "⍾"

  val FRUIT_CODES: Seq[String] = Seq("①", "⓶", "⓷", "⓸", "⓹", "⓺", "⓻", "⓼")
}
