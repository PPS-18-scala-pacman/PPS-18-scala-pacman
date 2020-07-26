package it.unibo.scalapacman.lib.prolog

import alice.tuprolog.Term

trait Termable {
  def toProlog: Term
}
