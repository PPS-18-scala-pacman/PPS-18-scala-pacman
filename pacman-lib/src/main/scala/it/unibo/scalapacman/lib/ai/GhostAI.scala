package it.unibo.scalapacman.lib.ai

import alice.tuprolog.Struct
import it.unibo.scalapacman.lib.Utility
import it.unibo.scalapacman.lib.prolog.Scala2P.{PrologEngine, convertibleToTerm, extractTerm, mkPrologEngine}
import it.unibo.scalapacman.lib.model.{Character, Direction, Ghost, Map, Pacman}
import it.unibo.scalapacman.lib.prolog.{Graph, GraphVertex, MinDistance}
import it.unibo.scalapacman.lib.engine.GameHelpers.CharacterHelper
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.Map.MapIndexes

object GhostAI {
  implicit val prologEngine: PrologEngine = mkPrologEngine(Utility.readFile(getClass.getResource("/prolog/Dijkstra.pl")))

  def shortestPath(character: Character, endTileIndexes: (Int, Int))(implicit engine: PrologEngine, map: Map): List[MapIndexes] = {
    val graph = Graph.fromMap(map).filterWalkable(character)
    val tileStart = GraphVertex(character.tileIndexes)
    val tileEnd = GraphVertex(endTileIndexes)
    val quest = MinDistance(graph, tileStart, tileEnd)

    engine(quest).headOption
      .map(extractTerm(_, 3))
      .map { case s: Struct => s.listIterator }.map(Utility.iteratorToList(_)).get
      .map(GraphVertex.fromTerm).map(_.tileIndexes)
  }

  def desiredDirection(ghost: Ghost, pacman: Pacman)(implicit engine: PrologEngine, map: Map): Direction =
    Option(shortestPath(ghost, pacman.tileIndexes)(engine, map).take(2)) filter(_.size == 2) map directionByPath getOrElse ghost.direction

  private def directionByPath(path: List[MapIndexes]): Direction = path match {
    case (x, _) :: (x1, _) :: Nil if x < x1 => Direction.EAST
    case (x, _) :: (x1, _) :: Nil if x > x1 => Direction.WEST
    case (_, y) :: (_, y1) :: Nil if y < y1 => Direction.SOUTH
    case (_, y) :: (_, y1) :: Nil if y > y1 => Direction.NORTH
  }
}
