package it.unibo.scalapacman.lib.ai

import alice.tuprolog.{Struct, Term}
import it.unibo.scalapacman.lib.Utility
import it.unibo.scalapacman.lib.prolog.Scala2P.{PrologEngine, convertibleToTerm, extractTerm, mkPrologEngine}
import it.unibo.scalapacman.lib.model.{Character, Direction, Map, MapType}
import it.unibo.scalapacman.lib.prolog.{Graph, GraphVertex, ShortestPath, ShortestPathClassic}
import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper, MapHelper}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.Map.MapIndexes

object GhostAI {

  implicit val prologEngine: PrologEngine = mkPrologEngine(
    Utility.readFile(getClass.getResource("/prolog/ShortestPath.pl")),
    Utility.readFile(getClass.getResource("/prolog/Dijkstra.pl")),
    Utility.readFile(getClass.getResource("/prolog/Maps.pl"))
  )

  def shortestPath(character: Character, endTileIndexes: MapIndexes)(implicit engine: PrologEngine, map: Map): List[MapIndexes] = {
    val graph = Graph.fromMap(map).filterWalkable(character)
    val quest: (GraphVertex,GraphVertex)=>Term = (tileStart, tileEnd) => ShortestPath(graph, tileStart, tileEnd)
    calculatePath(character.tileIndexes, endTileIndexes, quest, 3)(engine)
  }

  def shortestPathClassic(startTileIndexes: MapIndexes, endTileIndexes: MapIndexes)(implicit engine: PrologEngine): List[MapIndexes] = {
    val quest: (GraphVertex,GraphVertex)=>Term = (tileStart, tileEnd) => ShortestPathClassic(tileStart, tileEnd)
    calculatePath(startTileIndexes, endTileIndexes, quest, 3)(engine)
  }

  private def calculatePath(startTileIndexes: MapIndexes, endTileIndexes: MapIndexes, quest:(GraphVertex,GraphVertex)=>Term, index:Int)
                           (implicit engine: PrologEngine): List[MapIndexes] = {

    val tileStart = GraphVertex(startTileIndexes)
    val tileEnd = GraphVertex(endTileIndexes)

    engine(quest(tileStart, tileEnd)).headOption
      .map(extractTerm(_, index))
      .map { case s: Struct => s.listIterator }.map(Utility.iteratorToList(_)).getOrElse(Nil)
      .map(GraphVertex.fromTerm).map(_.tileIndexes)
  }

  def desiredDirection(ghost: Ghost, pacman: Pacman)(implicit engine: PrologEngine, map: Map): Direction =
    Option(shortestPath(ghost, pacman.tileIndexes)(engine, map)) collect { case List(a, b, _*) => (a, b) } map Direction.byPath getOrElse ghost.direction

  def desiredDirectionClassic(char: Character, endTileIndexes: MapIndexes)(implicit engine: PrologEngine): Option[Direction] = {
    implicit val map: Map = Map.create(MapType.CLASSIC)
    shortestPathClassic(char.tileIndexes, endTileIndexes)(engine) match {
      case List(tile1, tile2, _*) => Direction.byCrossTile((tile1, tile2), char)
      case _ => None
    }
  }

  def calculateDirectionClassic(self: Ghost, char: Character): Option[Direction] = {
    implicit val map: Map = Map.create(MapType.CLASSIC)
    val selfTile = self.tileIndexes

    if(self.tileIsCross) {
      char.nextCrossTile().flatMap ( charNextCross =>
        charNextCross match {
          case `selfTile` =>
            Direction.byCrossTile((selfTile, char.revert.nextCrossTile().get), char.revert)
          case _ if map.tileNearbyCrossings(charNextCross, char).contains(selfTile) =>
            Direction.byCrossTile((selfTile, charNextCross), char)
          case _ =>
            GhostAI.desiredDirectionClassic(self, charNextCross)
        }
      )
    } else {
      self.directionForTurn
    }
  }
}
