package it.unibo.scalapacman.client.map

import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit, GhostType}

object ElementsCode {
  /* Pacman */
  /**
   * Rappresenta Pacman rivolto verso l'alto
   * Carattere U+15E2 */
  val PACMAN_UP_CODE = "ᗢ"
  /**
   * Rappresenta Pacman rivolto verso il basso
   * Carattere U+15E3 */
  val PACMAN_DOWN_CODE = "ᗣ"
  /**
   * Rappresenta Pacman rivolto verso destra
   * Carattere U+15E7 */
  val PACMAN_RIGHT_CODE = "ᗧ"
  /**
   * Rappresenta Pacman rivolto verso sinistra
   * Carattere U+15E4 */
  val PACMAN_LEFT_CODE = "ᗤ"

  /**
   * Mappa che associa per ogni valore dell'enum Direction un codice di Pacman
   */
  val PACMAN_CODES_MAP: Map[Direction.Value, String] = Map(
    Direction.NORTH -> PACMAN_UP_CODE,
    Direction.SOUTH -> PACMAN_DOWN_CODE,
    Direction.EAST -> PACMAN_RIGHT_CODE,
    Direction.WEST -> PACMAN_LEFT_CODE
  )

  /* Spazio vuoto */
  /**
   * Rappresenta uno spazio vuoto
   * Carattere U+3000 */
  val EMPTY_SPACE_CODE = "　"

  /* Dot ed Energized Dot */
  /**
   * Rappresenta un dot
   * Carattere U+30FB */
  val DOT_CODE = "・"
  /**
   * Rappresenta un super dot
   * Carattere U+2742 */
  val ENERGIZER_DOT_CODE = "❂"

  /**
   * Mappa che associa per ogni valore dell'enum Dot un codice di un Dot
   */
  val DOT_CODES_MAP: Map[Dot.Value, String] = Map(
    Dot.SMALL_DOT -> DOT_CODE,
    Dot.ENERGIZER_DOT -> ENERGIZER_DOT_CODE
  )

  /* Muri */
  /**
   * Rappresenta un pezzo di muro
   * 2 caratteri U+2558 */
  val WALL_CODE = "██"

  /* Fantasmi */
  /**
   * Rappresenta fantasma Blinky
   * Carattere B, occupa 1 spazio */
  val GHOST_CODE_BLINKY = "B"
  /**
   * Rappresenta fantasma Pinky
   * Carattere P, occupa 1 spazio */
  val GHOST_CODE_PINKY = "P"
  /**
   * Rappresenta fantasma Inky
   * Carattere I, occupa 1 spazio */
  val GHOST_CODE_INKY = "I"
  /**
   * Rappresenta fantasma Clyde
   * Carattere C, occupa 1 spazio */
  val GHOST_CODE_CLYDE   = "C"

  /**
   * Mappa che associa ad ogni valore dell'enum GhostType un codice di un fantasma
   */
  val GHOST_CODES_MAP: Map[GhostType.Value, String] = Map(
    GhostType.BLINKY -> GHOST_CODE_BLINKY,
    GhostType.PINKY -> GHOST_CODE_PINKY,
    GhostType.INKY -> GHOST_CODE_INKY,
    GhostType.CLYDE -> GHOST_CODE_CLYDE,
  )

  /* Frecce */
  /**
   * Rappresenta freccia direzionale su
   * Carattere freccia su, occupa 1 spazio */
  val ARROW_UP_CODE = "\uFFEA"
  /**
   * Rappresenta freccia direzionale giu
   * Carattere freccia giu, occupa 1 spazio */
  val ARROW_DOWN_CODE = "\uFFEC"
  /**
   * Rappresenta freccia direzionale destar
   * Carattere freccia destra, occupa 1 spazio */
  val ARROW_RIGHT_CODE = "\uFFEB"
  /**
   * Rappresenta freccia direzionale sinistra
   * Carattere freccia sinistra, occupa 1 spazio */
  val ARROW_LEFT_CODE = "\uFFE9"

  /**
   * Mappa che associa ad ogni valore dell'enum Direction un codice di una freccia
   */
  val ARROW_CODES_MAP: Map[Direction.Value, String] = Map(
    Direction.NORTH -> ARROW_UP_CODE,
    Direction.SOUTH -> ARROW_DOWN_CODE,
    Direction.EAST -> ARROW_RIGHT_CODE,
    Direction.WEST -> ARROW_LEFT_CODE,
  )

  /* Frutti */
  /**
   * Rappresenta frutto ciliegie
   * Carattere U+2460 */
  val CHERRIES_CODE = "①"
  /**
   * Rappresenta frutto fragola
   * Carattere U+2461 */
  val STRAWBERRY_CODE = "⓶"
  /**
   * Rappresenta frutto pesca
   * Carattere U+2462 */
  val PEACH_CODE = "⓷"
  /**
   * Rappresenta frutto mela
   * Carattere U+2463 */
  val APPLE_CODE = "⓸"
  /**
   * Rappresenta frutto uva
   * Carattere U+2464 */
  val GRAPES_CODE = "⓹"
  /**
   * Rappresenta frutto galaxian
   * Carattere U+2465 */
  val GALAXIAN_CODE = "⓺"
  /**
   * Rappresenta frutto campana
   * Carattere U+2466 */
  val BELL_CODE = "⓻"
  /**
   * Rappresenta frutto chiave
   * Carattere U+2467 */
  val KEY_CODE = "⓼"

  /**
   * Mappa che associa ad ogni valore dell'Enum Fruit un codice di un frutto
   */
  val FRUIT_CODES_MAP: Map[Fruit.Value, String] = Map(
    Fruit.CHERRIES -> CHERRIES_CODE,
    Fruit.STRAWBERRY -> STRAWBERRY_CODE,
    Fruit.PEACH -> PEACH_CODE,
    Fruit.APPLE -> APPLE_CODE,
    Fruit.GRAPES -> GRAPES_CODE,
    Fruit.GALAXIAN -> GALAXIAN_CODE,
    Fruit.BELL -> BELL_CODE,
    Fruit.KEY -> KEY_CODE
  )

  /**
   * Controlla se code è uno dei codici usati per rappresentare Pacman
   * @param code  codice da controllare
   * @return true se è uno dei codici usati per rappresentare Pacman, false altrimenti
   */
  def matchPacman(code: String): Boolean = matchCode(code, PACMAN_CODES_MAP.values.toList)

  /**
   * Controlla se code è uno dei codici usati per rappresentare uno dei fantasmi
   * @param code  codice da controllare
   * @return true se è uno dei codici usati per rappresentare uno dei fantasmi, false altrimenti
   */
  def matchGhost(code: String): Boolean = matchCode(code, GHOST_CODES_MAP.values.toList)

  /**
   * Controlla se code è uno dei codici usati per rappresentare un frutto
   * @param code  codice da controllare
   * @return true se è uno dei codici usati per rappresentare un frutto, false altrimenti
   */
  def matchFruit(code: String): Boolean = matchCode(code, FRUIT_CODES_MAP.values.toList)

  /**
   * Controlla se elem fa parte della lista list
   * @param elem  elemento da controllare
   * @param list  lista su cui fare la valutazione
   * @return true se elem fa parte di list, false altrimenti
   */
  private def matchCode(elem: String, list: List[String]): Boolean = list.contains(elem)
}
