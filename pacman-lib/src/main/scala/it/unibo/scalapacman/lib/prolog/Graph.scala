package it.unibo.scalapacman.lib.prolog

import alice.tuprolog.{Struct, Term, Int => PrologInt}
import it.unibo.scalapacman.lib.model.{Character, Map}
import it.unibo.scalapacman.lib.engine.GameHelpers.MapHelper
import it.unibo.scalapacman.lib.model.Map.MapIndexes

case class Graph(nodes: List[GraphNode]) extends TermConvertible {
  def toTerm: Term = new Struct(nodes.map(_.toTerm).toArray)

  def filterWalkable(character: Character)(implicit map: Map): Graph =
    Graph(nodes.map(n => n.copy(neighbourhood = n.neighbourhood.filter(t => map.tile(t.vertex.tileIndexes).walkable(character)))))
}

case class GraphNode(vertex: GraphVertex, neighbourhood: List[GraphArc]) extends TermConvertible {
  def toTerm: Term = new Struct("-", vertex.toTerm, new Struct(neighbourhood.map(_.toTerm).toArray))
}

case class GraphVertex(tileIndexes: MapIndexes) extends TermConvertible {
  def toTerm: Term = new Struct("t", new PrologInt(tileIndexes._1), new PrologInt(tileIndexes._2))
}

case class GraphArc(vertex: GraphVertex, weight: Int = 1) extends TermConvertible {
  def toTerm: Term = new Struct("-", vertex.toTerm, new PrologInt(weight))
}

object Graph {
  def fromMap(implicit map: Map): Graph = Graph((
    for (
      y <- 0 until map.height;
      x <- 0 until map.width
    ) yield GraphNode(GraphVertex((x, y)), tileNeighboursArcs((x, y))
    )) toList)

  private def tileNeighboursArcs(pos: MapIndexes)(implicit map: Map): List[GraphArc] = tileNeighbours(pos) map (GraphVertex(_)) map (GraphArc(_))

  private def tileNeighbours(tileIndexes: MapIndexes)(implicit map: Map): List[MapIndexes] =
    ((1, 0) :: (-1, 0) :: (0, 1) :: (0, -1) :: Nil)
      .map(p => (p._1 + tileIndexes._1, p._2 + tileIndexes._2))
      .map(map.tileIndexes)
}

object GraphVertex {
  def fromTerm(term: Term): GraphVertex = term match {
    case s: Struct => fromStruct(s)
  }

  def fromStruct(struct: Struct): GraphVertex = fromIndexes(struct.getArg(0).asInstanceOf[PrologInt], struct.getArg(1).asInstanceOf[PrologInt])

  def fromIndexes(indexes: (PrologInt, PrologInt)): GraphVertex = GraphVertex((indexes._1.intValue, indexes._2.intValue))
}
