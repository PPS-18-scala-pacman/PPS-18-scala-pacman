package it.unibo.scalapacman.client.map

import it.unibo.scalapacman.common.GameEntityDTO
import it.unibo.scalapacman.lib.model.{Character, Fruit, Map}
import it.unibo.scalapacman.lib.engine.GameHelpers.CharacterHelper
import it.unibo.scalapacman.lib.model.Character.Ghost
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.Dot.{ENERGIZER_DOT, SMALL_DOT}
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.lib.model.Tile.{GhostSpawn, Track, TrackSafe, Wall}

/**
 * Contiene funzioni di utility per la rappresentazione dell'oggetto Map di Pacman-lib
 * nel formato che viene utilizzato dalla view per mostrarla a video
 */
object PacmanMap {
  type PacmanMap = List[List[String]]

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
      .foldLeft(pacmanMap)((pacmanMap, ghost) => addCharacter(getGhostCompleteCode(ghost))(pacmanMap, ghost))

    // Stampo pacman per ultimo E se non è morto
    pacmanMap = gameEntities.map(entity => entity.toPacman)
      .collect { case Some(pacman) if !pacman.isDead => pacman }
      .foldLeft(pacmanMap)((pacmanMap, pacman) => addCharacter(getPacmanCode(pacman.direction))(pacmanMap, pacman))

    pacmanMap
  }

  /**
   * Trasforma la mappa della Pacman-lib da una matrice di Tile ad una matrice di String
   * @param map la mappa da convertire
   * @return la mappa convertita
   */
  def toPacmanMap(map: Map): PacmanMap = map.tiles map (row => row map {
    case Wall() => ElementsCode.WALL_CODE
    case GhostSpawn() | TrackSafe(None) | Track(None) => ElementsCode.EMPTY_SPACE_CODE
    case Track(Some(SMALL_DOT)) => ElementsCode.DOT_CODE
    case Track(Some(ENERGIZER_DOT)) => ElementsCode.ENERGIZER_DOT_CODE
    case tile@(Track(Some(Fruit.Fruit(_))) | TrackSafe(Some(Fruit.Fruit(_)))) => ElementsCode.FRUIT_CODES_MAP(tile.eatable.get.asInstanceOf[Fruit.Fruit])
    case _ => ElementsCode.EMPTY_SPACE_CODE
  })

  private def addCharacter(elementCode: String)(pacmanMap: PacmanMap, character: Character)(implicit map: Map): PacmanMap =
    addElement(pacmanMap, elementCode, character.tileIndexes)

  private def addElement(pacmanMap: PacmanMap, element: String, position: (Int, Int)): PacmanMap =
    pacmanMap updated (position._2, pacmanMap(position._2) updated (position._1, element))

  private def getPacmanCode(direction: Direction): String = ElementsCode.PACMAN_CODES_MAP(direction)

  private def getGhostCompleteCode(ghost: Ghost): String = getGhostCode(ghost.ghostType) + getArrowCode(ghost.direction)

  private def getGhostCode(ghostType: GhostType): String = ElementsCode.GHOST_CODES_MAP(ghostType)

  private def getArrowCode(direction: Direction): String = ElementsCode.ARROW_CODES_MAP(direction)
}
