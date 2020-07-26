package it.unibo.scalapacman.lib.ai

import alice.tuprolog.{Struct, Term}
import it.unibo.scalapacman.lib.Utility
import it.unibo.scalapacman.lib.prolog.Scala2P.{extractTerm, mkPrologEngine, termableToTerm}
import it.unibo.scalapacman.lib.model.{Character, Map}
import it.unibo.scalapacman.lib.prolog.{Graph, GraphVertex, MinDistance}
import it.unibo.scalapacman.lib.engine.GameHelpers.MapHelper

object GhostAI {
  implicit val engine: Term => Stream[Term] = mkPrologEngine(Utility.readFile(getClass.getResource("/prolog/Dijkstra.pl")))

  def shortestPath(character: Character, endTileIndexes: (Int, Int))(implicit engine: Term => Stream[Term], map: Map): List[(Int, Int)] = {
    val graph = Graph.fromMap(map).filterWalkable(character)
    val tileStart = GraphVertex(map.tileIndexes(character.position))
    val tileEnd = GraphVertex(endTileIndexes)
    val quest = MinDistance(graph, tileStart, tileEnd)
    // println(quest)

    engine(quest).headOption
      .map(extractTerm(_, 3))
      .map { case s: Struct => s.listIterator }.map(Utility.iteratorToList(_)).get
      .map(GraphVertex.fromTerm).map(_.tileIndexes)
  }
}
