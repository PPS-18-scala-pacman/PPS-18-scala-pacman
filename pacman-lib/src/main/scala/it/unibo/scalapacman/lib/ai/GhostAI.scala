package it.unibo.scalapacman.lib.ai

import alice.tuprolog.{Struct, Term}
import it.unibo.scalapacman.lib.Utility
import it.unibo.scalapacman.lib.prolog.Scala2P.{PrologEngine, convertibleToTerm, extractTerm, mkPrologEngine}
import it.unibo.scalapacman.lib.model.{Character, Direction, Ghost, Map, Pacman}
import it.unibo.scalapacman.lib.prolog.{Graph, GraphVertex, MinDistance, MinDistanceClassic}
import it.unibo.scalapacman.lib.engine.GameHelpers.CharacterHelper
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.Map.MapIndexes
import it.unibo.scalapacman.lib.Utility.directionByPath

object GhostAI {
  //TODO STO COSO È UNA GRAN CAZZATA SECONDO ME
  implicit val prologEngine: PrologEngine = mkPrologEngine(Utility.readFile(getClass.getResource("/prolog/Dijkstra.pl")))

  def shortestPath(character: Character, endTileIndexes: MapIndexes)(implicit engine: PrologEngine, map: Map): List[MapIndexes] = {
    val graph = Graph.fromMap(map).filterWalkable(character)
    val quest: (GraphVertex,GraphVertex)=>Term = (tileStart, tileEnd) => MinDistance(graph, tileStart, tileEnd)
    calculatePath(character.tileIndexes, endTileIndexes, quest)(engine, map)
  }

  def shortestPathClassic(startTileIndexes: MapIndexes, endTileIndexes: MapIndexes)(implicit engine: PrologEngine, map: Map): List[MapIndexes] = {
    val quest: (GraphVertex,GraphVertex)=>Term = (tileStart, tileEnd) => MinDistanceClassic(tileStart, tileEnd)
    calculatePath(startTileIndexes, endTileIndexes, quest)(engine, map)
  }

  private def calculatePath(startTileIndexes: MapIndexes, endTileIndexes: MapIndexes, quest:(GraphVertex,GraphVertex)=>Term)
                           (implicit engine: PrologEngine, map: Map): List[MapIndexes] = {

    val tileStart = GraphVertex(startTileIndexes)
    val tileEnd = GraphVertex(endTileIndexes)

    engine(quest(tileStart, tileEnd)).headOption
      .map(extractTerm(_, 2))
      .map { case s: Struct => s.listIterator }.map(Utility.iteratorToList(_)).getOrElse(Nil)
      .map(GraphVertex.fromTerm).map(_.tileIndexes)
  }

  def desiredDirection(ghost: Ghost, pacman: Pacman)(implicit engine: PrologEngine, map: Map): Direction =
    Option(shortestPath(ghost, pacman.tileIndexes)(engine, map).take(2)) filter(_.size == 2) map directionByPath getOrElse ghost.direction

  def desiredDirectionClassic(char: Character, endTileIndexes: MapIndexes)(implicit engine: PrologEngine, map: Map): Option[Direction] = {
    shortestPathClassic(char.tileIndexes, endTileIndexes)(engine, map) match {
      case path:List[MapIndexes] if path.size < 2 => None
      case path:List[MapIndexes] => Direction.windRose.find(dir =>
        char.nextTile(dir).walkable(char) && char.nextCrossTile(path.head, dir).contains(path.tail.head))
    }
  }

}
