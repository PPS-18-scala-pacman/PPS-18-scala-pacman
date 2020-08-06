package it.unibo.scalapacman.lib.prolog

import alice.tuprolog.{Struct, Term, Int => PrologInt}
import it.unibo.scalapacman.lib.model.{Character, Map}
import it.unibo.scalapacman.lib.engine.GameHelpers.MapHelper
import it.unibo.scalapacman.lib.model.Map.MapIndexes

/**
 * Graph composed by nodes and directional arcs
 *
 * @param nodes nodes with a vertex and its arcs
 */
case class Graph(nodes: List[GraphNode]) extends TermConvertible {
  def toTerm: Term = new Struct(nodes.map(_.toTerm).toArray)

  def filterWalkable(character: Character)(implicit map: Map): Graph = Graph(
    nodes
      filter(n => map.tile(n.vertex.tileIndexes).walkable(character))
      map(n => n.copy(neighbourhood = n.neighbourhood.filter(t => map.tile(t.vertex.tileIndexes).walkable(character))))
  )
}

/**
 * Node of the graph, composed by a vertex and its neighbourhood
 * Example: t(x, y)-[t(x1, y1)-weight1, t(x2, y2)-weight2, ...]
 *
 * @param vertex node's vertex
 * @param neighbourhood node's neighbourhood
 */
case class GraphNode(vertex: GraphVertex, neighbourhood: List[GraphArc]) extends TermConvertible {
  def toTerm: Term = new Struct("-", vertex.toTerm, new Struct(neighbourhood.map(_.toTerm).toArray))
}

/**
 * Vertex of the graph
 * Example: t(x, y)
 *
 * @param tileIndexes Indexes of the relative tile in the map
 */
case class GraphVertex(tileIndexes: MapIndexes) extends TermConvertible {
  def toTerm: Term = new Struct("t", new PrologInt(tileIndexes._1), new PrologInt(tileIndexes._2))
}

/**
 * Directional arc to a vertex with a weight
 * Example: t(x, y)-weight
 *
 * @param vertex vertex at the end of the arc
 * @param weight weight of the arc
 */
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

  private def tileNeighboursArcs(pos: MapIndexes)(implicit map: Map): List[GraphArc] = map tileNeighboursIndexes pos map (GraphVertex(_)) map (GraphArc(_))
}

object GraphVertex {
  def fromTerm(term: Term): GraphVertex = term match {
    case s: Struct => fromStruct(s)
  }

  def fromStruct(struct: Struct): GraphVertex = fromIndexes(struct.getArg(0).asInstanceOf[PrologInt], struct.getArg(1).asInstanceOf[PrologInt])

  def fromIndexes(indexes: (PrologInt, PrologInt)): GraphVertex = GraphVertex((indexes._1.intValue, indexes._2.intValue))
}
