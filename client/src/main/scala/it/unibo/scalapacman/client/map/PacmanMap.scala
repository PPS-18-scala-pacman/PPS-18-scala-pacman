package it.unibo.scalapacman.client.map

import it.unibo.scalapacman.common.{DotDTO, FruitDTO, UpdateModelDTO}
import it.unibo.scalapacman.lib.model.{Character, Dot, Fruit, Ghost, Map}
import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper, MapHelper}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.Dot.{ENERGIZER_DOT, SMALL_DOT}
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.lib.model.Tile.{GhostSpawn, Track, TrackSafe, Wall}

object PacmanMap {
  type PacmanMap = List[List[String]]

  def createMap(map: Map, model: UpdateModelDTO): PacmanMap = {
    var pacmanMap: PacmanMap = toPacmanMap(map)
    implicit val mapRef: Map = map

    // Mappa con frutti/dot aggiornati dovrebbe essere fatto nel common con qualche funzionalità
    pacmanMap = model.dots.foldLeft(pacmanMap)((pacmanMap, dot) => addDot(getDotCode(dot.dotHolder.dot))(pacmanMap, dot))

    pacmanMap = model.fruit.map(fruit => addFruit(getFruitCode(fruit.fruitHolder.fruit))(pacmanMap, fruit)) getOrElse pacmanMap

    pacmanMap = model.gameEntities.map(entity => entity.toGhost)
      .collect { case Some(ghost) => ghost }
      .foldLeft(pacmanMap)((pacmanMap, ghost) => addCharacter(getGhostCompleteCode(ghost))(pacmanMap, ghost))

    // Stampo pacman per ultimo E se non è morto
    pacmanMap = model.gameEntities.map(entity => entity.toPacman)
      .collect { case Some(pacman) if !pacman.isDead => pacman }
      .foldLeft(pacmanMap)((pacmanMap, pacman) => addCharacter(getPacmanCode(pacman.direction))(pacmanMap, pacman))

    pacmanMap
  }

  def toPacmanMap(map: Map): PacmanMap = map.tiles map (row => row map {
    case Wall() => ElementsCode.WALL_CODE
    case GhostSpawn() | TrackSafe() | Track(None) | Track(Some(SMALL_DOT)) | Track(Some(ENERGIZER_DOT)) => ElementsCode.EMPTY_SPACE_CODE
    case _ => ElementsCode.EMPTY_SPACE_CODE
  })

  private def addCharacter(elementCode: String)(pacmanMap: PacmanMap, character: Character)(implicit map: Map): PacmanMap =
    addElement(pacmanMap, elementCode, character.tileIndexes)

  private def addDot(elementCode: String)(pacmanMap: PacmanMap, dot: DotDTO)(implicit map: Map): PacmanMap =
    addElement(pacmanMap, elementCode, map.tileIndexes(dot.pos))

  private def addFruit(elementCode: String)(pacmanMap: PacmanMap, fruit: FruitDTO)(implicit map: Map): PacmanMap =
    addElement(pacmanMap, elementCode, map.tileIndexes(fruit.pos))

  private def addElement(pacmanMap: PacmanMap, element: String, position: (Int, Int)): PacmanMap =
    pacmanMap updated (position._2, pacmanMap(position._2) updated (position._1, element))

  private def getPacmanCode(direction: Direction): String = ElementsCode.PACMAN_CODES_MAP(direction)

  private def getGhostCompleteCode(ghost: Ghost): String = getGhostCode(ghost.ghostType) + getArrowCode(ghost.direction)

  private def getGhostCode(ghostType: GhostType): String = ElementsCode.GHOST_CODES_MAP(ghostType)

  private def getDotCode(dot: Dot.Value): String = ElementsCode.DOT_CODES_MAP(dot)

  private def getFruitCode(fruit: Fruit.Value): String = ElementsCode.FRUIT_CODES_MAP(fruit)

  private def getArrowCode(direction: Direction): String = ElementsCode.ARROW_CODES_MAP(direction)
}
