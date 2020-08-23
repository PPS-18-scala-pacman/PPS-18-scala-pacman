package it.unibo.scalapacman.lib.prolog

import alice.tuprolog.{Struct, Term, Var}

case class ShortestPath(graph: Graph, tileStart: GraphVertex, tileEnd: GraphVertex) extends TermConvertible {
  /**
   * Something like "min_dist([ t0-[t1-1], t1-[t2-1, t3-1], t2-[], t3-[] ], t0, t3, X)"
   */
  override def toTerm: Term = new Struct("shortest_path", graph.toTerm, tileStart.toTerm, tileEnd.toTerm, new Var())
}

case class ShortestPathClassic(tileStart: GraphVertex, tileEnd: GraphVertex) extends TermConvertible {
  /**
   * Something like "min_dist(t(6,1), t(15,29), X)" on classic pacman map
   */
  override def toTerm: Term = new Struct("shortest_path", tileStart.toTerm, tileEnd.toTerm, new Var())
}
