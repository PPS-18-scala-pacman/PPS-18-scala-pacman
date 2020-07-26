package it.unibo.scalapacman.lib.prolog

import alice.tuprolog.{Struct, Term, Var}

case class MinDistance(graph: Graph, tileStart: GraphVertex, tileEnd: GraphVertex) extends Termable {
  /**
   * Something like "min_dist([ t0-[t1-1], t1-[t2-1, t3-1], t2-[], t3-[] ], t0, t3, X)"
   */
  override def toProlog: Term =
    new Struct("min_dist", graph.toProlog, tileStart.toProlog, tileEnd.toProlog, new Var())
}
