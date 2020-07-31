package it.unibo.scalapacman.client.map

import it.unibo.scalapacman.common.{Item, Pellet, UpdateModel}
import it.unibo.scalapacman.lib.model.{Character, Direction, Dot, Fruit, Map}
import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper, MapHelper}
import it.unibo.scalapacman.lib.model.Direction.Direction

object PacmanMap {
  type PacmanMap = List[List[String]]

  def createMap(model: UpdateModel): PacmanMap = {
    var pacmanMap: PacmanMap = MapBuilder.buildClassic()
    implicit val classicMap: Map = MapBuilder.mapClassic

    pacmanMap = model.pellets.foldLeft(pacmanMap)((pacmanMap, dot) => addDot(getDotCode(dot.pelletType.dot))(pacmanMap, dot))

    pacmanMap = model.fruit.map(fruit => addFruit(getFruitCode(fruit.id.fruit))(pacmanMap, fruit)) getOrElse pacmanMap

    pacmanMap = model.gameEntities.map(entity => entity.toGhost)
      .collect { case Some(ghost) => ghost }
      .foldLeft(pacmanMap)(addCharacter(ElementsCode.GHOST_CODE))

    // Stampo pacman per ultimo E se non è morto
    pacmanMap = model.gameEntities.map(entity => entity.toPacman)
      .collect { case Some(pacman) if !pacman.isDead => pacman }
      .foldLeft(pacmanMap)((pacmanMap, pacman) => addCharacter(getPacmanCode(pacman.direction))(pacmanMap, pacman))

    pacmanMap
  }

  def addCharacter(elementCode: String)(pacmanMap: PacmanMap, character: Character)(implicit map: Map): PacmanMap =
    addElement(pacmanMap, elementCode, character.tileIndexes)

  def addDot(elementCode: String)(pacmanMap: PacmanMap, pellet: Pellet)(implicit map: Map): PacmanMap =
    addElement(pacmanMap, elementCode, map.tileIndexes(pellet.pos))

  def addFruit(elementCode: String)(pacmanMap: PacmanMap, fruit: Item)(implicit map: Map): PacmanMap =
    addElement(pacmanMap, elementCode, map.tileIndexes(fruit.pos))

  def addElement(pacmanMap: PacmanMap, element: String, position: (Int, Int)): PacmanMap =
    pacmanMap updated (position._1, pacmanMap(position._1) updated (position._2, element))

  def getPacmanCode(direction: Direction): String = direction match {
    case Direction.NORTH => ElementsCode.PACMAN_UP_CODE
    case Direction.SOUTH => ElementsCode.PACMAN_DOWN_CODE
    case Direction.WEST => ElementsCode.PACMAN_RIGHT_CODE
    case Direction.EAST => ElementsCode.PACMAN_LEFT_CODE
  }

  def getDotCode(dot: Dot.Value): String = dot match {
    case Dot.SMALL_DOT => ElementsCode.DOT_CODE
    case Dot.ENERGIZER_DOT => ElementsCode.ENERGIZER_DOT_CODE
  }

  def getFruitCode(fruit: Fruit.Value): String = ElementsCode.FRUIT_CODES_MAP(fruit)
}
