package it.unibo.scalapacman.lib.ai

import alice.tuprolog.{Struct, Term}
import it.unibo.scalapacman.lib.Utility
import it.unibo.scalapacman.lib.prolog.Scala2P.{PrologEngine, convertibleToTerm, extractTerm, mkPrologEngine}
import it.unibo.scalapacman.lib.model.{Character, Ghost, Map, Pacman}
import it.unibo.scalapacman.lib.prolog.{Graph, GraphVertex, MinDistance, MinDistanceClassic}
import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper, MapHelper}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.Map.MapIndexes
import it.unibo.scalapacman.lib.Utility.{directionByCrossTile, directionByPath}

object GhostAI {

  implicit val prologEngine: PrologEngine = mkPrologEngine(Utility.readFile(getClass.getResource("/prolog/Dijkstra.pl")))

  def shortestPath(character: Character, endTileIndexes: MapIndexes)(implicit engine: PrologEngine, map: Map): List[MapIndexes] = {
    val graph = Graph.fromMap(map).filterWalkable(character)
    val quest: (GraphVertex,GraphVertex)=>Term = (tileStart, tileEnd) => MinDistance(graph, tileStart, tileEnd)
    calculatePath(character.tileIndexes, endTileIndexes, quest, 3)(engine)
  }

  def shortestPathClassic(startTileIndexes: MapIndexes, endTileIndexes: MapIndexes)(implicit engine: PrologEngine): List[MapIndexes] = {
    val quest: (GraphVertex,GraphVertex)=>Term = (tileStart, tileEnd) => MinDistanceClassic(tileStart, tileEnd)
    calculatePath(startTileIndexes, endTileIndexes, quest, 2)(engine)
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
    Option(shortestPath(ghost, pacman.tileIndexes)(engine, map).take(2)) filter(_.size == 2) map directionByPath getOrElse ghost.direction

  def desiredDirectionClassic(char: Character, endTileIndexes: MapIndexes)(implicit engine: PrologEngine): Option[Direction] = {
    implicit val map: Map = Map.classic
    shortestPathClassic(char.tileIndexes, endTileIndexes)(engine) match {
      case path:List[MapIndexes] if path.size < 2 => None
      case path:List[MapIndexes] => directionByCrossTile(path, char)
    }
  }

  def calculateDirectionClassic(self: Ghost, char: Character):Option[Direction] = {
    implicit val map: Map = Map.classic
    val selfTile = self.tileIndexes

    if(self.tileIsCross) {
      char.nextCrossTile().flatMap ( charNextCross =>
        charNextCross match {
          case `selfTile` =>
            directionByCrossTile(selfTile :: char.revert.nextCrossTile().get :: Nil, char.revert)
          case _ if map.tileNearbyCrossings(charNextCross, char).contains(selfTile) =>
            directionByCrossTile(selfTile :: charNextCross :: Nil, char)
          case _ =>
            GhostAI.desiredDirectionClassic(self, charNextCross)
        }
      )
    } else {
      self.directionForTurn
    }
  }
}
