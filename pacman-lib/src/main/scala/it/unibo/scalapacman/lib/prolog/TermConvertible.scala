package it.unibo.scalapacman.lib.prolog

import alice.tuprolog.Term

trait TermConvertible {
  def toTerm: Term
}
