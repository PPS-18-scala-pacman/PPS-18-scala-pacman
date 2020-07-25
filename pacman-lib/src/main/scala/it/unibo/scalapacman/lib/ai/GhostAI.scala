package it.unibo.scalapacman.lib.ai

import alice.tuprolog.{Struct, Term}
import it.unibo.scalapacman.lib.Utility
import it.unibo.scalapacman.lib.engine.Scala2P.{extractTerm, mkPrologEngine, stringToTerm}
import it.unibo.scalapacman.lib.model.{Character, Map}
import it.unibo.scalapacman.lib.engine.GameHelpers.MapHelper

object GhostAI {
  implicit val engine: Term => Stream[Term] = mkPrologEngine(Utility.readFile(getClass.getResource("/prolog/Dijkstra.pl")))

  def shortestPath(character: Character, endTileIndexes: (Int, Int))(implicit engine: Term => Stream[Term], map: Map): List[(Int, Int)] = {
    //    val result = engine("min_dist([0-[1-1], 1-[2-1, 3-1], 2-[], 3-[]], 0, 3, X)")
    val startTileIndexes = map.tileIndexes(character.position)
    val graph = Graph.fromMap(map).filterWalkable(character).toProlog
    val tileStart = GraphVertice(startTileIndexes).toProlog
    val tileEnd = GraphVertice(endTileIndexes).toProlog
    println(s"min_dist($graph, $tileStart, $tileEnd, X)")
    val result = engine(s"min_dist($graph, $tileStart, $tileEnd, X)") map (extractTerm(_, 3))
    val result2 = result map { case s: Struct => s.listIterator } map (Utility.iteratorToList(_))
    result2.head.map(_.toString).map(Graph.nameToTilePosition)
  }

}

case class Graph(nodes: List[GraphNode]) {
  def toProlog: String = "[" + nodes.map(_.toProlog).mkString(",") + "]"

  def filterWalkable(character: Character)(implicit map: Map): Graph =
    Graph(nodes.map(n => n.copy(neighbourhood = n.neighbourhood.filter(t => map.tile(t.vertice.tileIndexes).walkable(character)))))
}

case class GraphNode(vertice: GraphVertice, neighbourhood: List[GraphArc]) {
  def toProlog: String = vertice.toProlog + "-[" + neighbourhood.map(_.toProlog).mkString(",") + "]"
}

case class GraphVertice(tileIndexes: (Int, Int)) {
  def toProlog: String = "t" + tileIndexes._1 + "_" + tileIndexes._2
}

case class GraphArc(vertice: GraphVertice, weight: Int = 1) {
  def toProlog: String = vertice.toProlog + "-1"
}

object Graph {
  def fromMap(map: Map): Graph = Graph((for (
    y <- 0 until map.height;
    x <- 0 until map.width
  ) yield GraphNode(GraphVertice((x, y)), tileNeighbours((x, y))(map).map(GraphVertice).map(GraphArc(_)))) toList)

  private def tileNeighbours(tileIndexes: (Int, Int))(implicit map: Map): List[(Int, Int)] =
    ((1, 0) :: (-1, 0) :: (0, 1) :: (0, -1) :: Nil)
      .map(p => (p._1 + tileIndexes._1, p._2 + tileIndexes._2))
      .map(map.tileIndexes)

  def nameToTilePosition(tileName: String): (Int, Int) = "t(\\d+)_(\\d+)".r.findAllIn(tileName).matchData.map {
    m => (m.group(1).toInt, m.group(2).toInt)
  }.toList.head
}
