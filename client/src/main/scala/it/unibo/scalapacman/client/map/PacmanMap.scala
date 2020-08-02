package it.unibo.scalapacman.client.map

import it.unibo.scalapacman.common.{FruitDTO, DotDTO, UpdateModelDTO}
import it.unibo.scalapacman.lib.model.{Character, Dot, Fruit, Ghost, Map}
import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper, MapHelper}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.GhostType.GhostType

object PacmanMap {
  type PacmanMap = List[List[String]]

  def createMap(model: UpdateModelDTO): PacmanMap = {
    var pacmanMap: PacmanMap = MapBuilder.buildClassic()
    implicit val classicMap: Map = MapBuilder.mapClassic

    // Mappa con frutti/pellet aggiornati dovrebbe essere fatto nel common con qualche funzionalità
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

  def addCharacter(elementCode: String)(pacmanMap: PacmanMap, character: Character)(implicit map: Map): PacmanMap =
    addElement(pacmanMap, elementCode, character.tileIndexes)

  def addDot(elementCode: String)(pacmanMap: PacmanMap, pellet: DotDTO)(implicit map: Map): PacmanMap =
    addElement(pacmanMap, elementCode, map.tileIndexes(pellet.pos))

  def addFruit(elementCode: String)(pacmanMap: PacmanMap, fruit: FruitDTO)(implicit map: Map): PacmanMap =
    addElement(pacmanMap, elementCode, map.tileIndexes(fruit.pos))

  def addElement(pacmanMap: PacmanMap, element: String, position: (Int, Int)): PacmanMap =
    pacmanMap updated (position._1, pacmanMap(position._1) updated (position._2, element))

  def getPacmanCode(direction: Direction): String = ElementsCode.PACMAN_CODES_MAP(direction)

  def getGhostCompleteCode(ghost: Ghost): String = getGhostCode(ghost.ghostType) + getArrowCode(ghost.direction)

  def getGhostCode(ghostType: GhostType): String = ElementsCode.GHOST_CODES_MAP(ghostType)

  def getDotCode(dot: Dot.Value): String = ElementsCode.DOT_CODES_MAP(dot)

  def getFruitCode(fruit: Fruit.Value): String = ElementsCode.FRUIT_CODES_MAP(fruit)

  def getArrowCode(direction: Direction): String = ElementsCode.ARROW_CODES_MAP(direction)
}
