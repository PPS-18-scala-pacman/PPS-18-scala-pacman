package it.unibo.scalapacman.client.map

import java.awt.Color

import it.unibo.scalapacman.client.gui.ElementStyle
import it.unibo.scalapacman.common.GameEntityDTO
import it.unibo.scalapacman.lib.model.{Character, Fruit, GameState, Map, Tile}
import it.unibo.scalapacman.lib.engine.GameHelpers.CharacterHelper
import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.Dot.{ENERGIZER_DOT, SMALL_DOT}
import it.unibo.scalapacman.lib.model.GhostType.{BLINKY, CLYDE, GhostType, INKY, PINKY}
import it.unibo.scalapacman.lib.model.PacmanType.{CAPMAN, MS_PACMAN, PACMAN, RAPMAN}
import it.unibo.scalapacman.lib.model.Tile.{GhostSpawn, Track, TrackSafe, Wall}

/**
 * Contiene funzioni di utility per la rappresentazione dell'oggetto Map di Pacman-lib
 * nel formato che viene utilizzato dalla view per mostrarla a video
 */
object PacmanMap {
  type PacmanMap = List[List[(String, Option[ElementStyle])]]

  val WALL_STYLE: ElementStyle = ElementStyle(Left(Color.BLUE))

  val PACMAN_STYLE: ElementStyle = ElementStyle(Left(Color.YELLOW))
  val MS_PACMAN_STYLE: ElementStyle = ElementStyle(Left(Color.ORANGE))
  val CAPMAN_STYLE: ElementStyle = ElementStyle(Left(Color.GREEN))
  val RAPMAN_STYLE: ElementStyle = ElementStyle(Left(Color.BLUE))

  private val GHOST_FEARED_COLOR = Color.LIGHT_GRAY
  private def ghostColor(primaryColor: Color)(gameState: GameState): Color = if (gameState.ghostInFear) GHOST_FEARED_COLOR else primaryColor
  val BLINKY_STYLE: ElementStyle = ElementStyle(Right(ghostColor(Color.RED)))
  val INKY_STYLE: ElementStyle = ElementStyle(Right(ghostColor(Color.CYAN)))
  val PINKY_STYLE: ElementStyle = ElementStyle(Right(ghostColor(Color.PINK)))
  val CLYDE_STYLE: ElementStyle = ElementStyle(Right(ghostColor(Color.ORANGE)))

  /**
   * Ritorna la mappa nella versione che sarà utilizzata poi dalla view, compresa la presenza dei personaggi
   * @param map la mappa attuale
   * @param gameEntities le informazioni della posizione e dello stato dei personaggi
   * @return la mappa convertita arricchita dei personaggi
   */
  def createWithCharacters(map: Map, gameEntities: Set[GameEntityDTO]): PacmanMap = {
    var pacmanMap: PacmanMap = toPacmanMap(map)
    implicit val mapRef: Map = map

    pacmanMap = gameEntities.map(entity => entity.toGhost)
      .collect { case Some(ghost) => ghost }
      .foldLeft(pacmanMap)((pacmanMap, ghost) => addCharacter(getGhostCompleteCode(ghost), retrieveStyle(ghost))(pacmanMap, ghost))

    // Stampo pacman per ultimo E se non è morto
    pacmanMap = gameEntities.map(entity => entity.toPacman)
      .collect { case Some(pacman) if !pacman.isDead => pacman }
      .foldLeft(pacmanMap)((pacmanMap, pacman) => addCharacter(getPacmanCode(pacman.direction), retrieveStyle(pacman))(pacmanMap, pacman))

    pacmanMap
  }

  /**
   * Trasforma la mappa della Pacman-lib da una matrice di Tile ad una matrice di String
   * @param map la mappa da convertire
   * @return la mappa convertita
   */
  def toPacmanMap(map: Map): PacmanMap = map.tiles map (row => row map(tile => (retrieveCode(tile), retrieveStyle(tile))))

  private def retrieveCode(tile: Tile): String = tile match {
    case Wall() => ElementsCode.WALL_CODE
    case GhostSpawn() | TrackSafe(None) | Track(None) => ElementsCode.EMPTY_SPACE_CODE
    case Track(Some(SMALL_DOT)) => ElementsCode.DOT_CODE
    case Track(Some(ENERGIZER_DOT)) => ElementsCode.ENERGIZER_DOT_CODE
    case Track(Some(Fruit.Fruit(_))) | TrackSafe(Some(Fruit.Fruit(_))) => ElementsCode.FRUIT_CODES_MAP(tile.eatable.get.asInstanceOf[Fruit.Fruit])
    case _ => ElementsCode.EMPTY_SPACE_CODE
  }

  private def retrieveStyle(tile: Tile): Option[ElementStyle] = tile match {
    case Wall() => Some(WALL_STYLE)
    case _ => None
  }

  private def retrieveStyle(character: Character): Option[ElementStyle] = character match {
    case Pacman(PACMAN, _, _, _, _) => Some(PACMAN_STYLE)
    case Pacman(MS_PACMAN, _, _, _, _) => Some(MS_PACMAN_STYLE)
    case Pacman(CAPMAN, _, _, _, _) => Some(CAPMAN_STYLE)
    case Pacman(RAPMAN, _, _, _, _) => Some(RAPMAN_STYLE)
    case Ghost(BLINKY, _, _, _, _) => Some(BLINKY_STYLE)
    case Ghost(INKY, _, _, _, _) => Some(INKY_STYLE)
    case Ghost(PINKY, _, _, _, _) => Some(PINKY_STYLE)
    case Ghost(CLYDE, _, _, _, _) => Some(CLYDE_STYLE)
    case _ => None
  }

  private def addCharacter(elementCode: (String, Option[ElementStyle]))(pacmanMap: PacmanMap, character: Character)(implicit map: Map): PacmanMap =
    addElement(pacmanMap, elementCode, character.tileIndexes)

  private def addElement(pacmanMap: PacmanMap, element: (String, Option[ElementStyle]), position: (Int, Int)): PacmanMap =
    pacmanMap updated (position._2, pacmanMap(position._2) updated (position._1, element))

  private def getPacmanCode(direction: Direction): String = ElementsCode.PACMAN_CODES_MAP(direction)

  private def getGhostCompleteCode(ghost: Ghost): String = getGhostCode(ghost.characterType) + getArrowCode(ghost.direction)

  private def getGhostCode(ghostType: GhostType): String = ElementsCode.GHOST_CODES_MAP(ghostType)

  private def getArrowCode(direction: Direction): String = ElementsCode.ARROW_CODES_MAP(direction)
}
