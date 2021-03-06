package it.unibo.scalapacman.lib.engine

import alice.tuprolog.{Struct, Term, Var}
import it.unibo.scalapacman.lib.Utility
import it.unibo.scalapacman.lib.prolog.Scala2P.{extractTerm, mkPrologEngine, seqToTerm, stringToTerm}
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.mutable

class Scala2PTest extends AnyWordSpec {

  def permutationAssert[A](p: List[A]): Unit = p foreach (n => assert(List(1, 2, 3).contains(n.asInstanceOf[alice.tuprolog.Int].intValue)))

  "Prolog engine" should {
    "create a space from a string" in {
      val engine: Term => Stream[Term] = mkPrologEngine(
        """
        member([H|T],H,T).
        member([H|T],E,[H|T2]):- member(T,E,T2).
        permutation([],[]).
        permutation(L,[H|TP]) :- member(L,H,T), permutation(T,TP).
      """)

      Left("permutation([1,2,3],L)") :: Right(new Struct("permutation", 1 to 3, new Var())) :: Nil foreach
        (struct => {
          val result = struct.fold(engine(_), engine(_))
          assert(result.size == 6)
          val permutations = result map (extractTerm(_, 1))
          permutations foreach (t => assert(t.isInstanceOf[Struct] && t.isList))
          permutations map (_.asInstanceOf[Struct].listIterator) map (Utility.iteratorToList(_)) foreach permutationAssert
        })
    }
    "create a space from a file" in {
      val clauses = Utility.readFile(getClass.getResource("/prolog/permutation.pl"))
      val engine: Term => Stream[Term] = mkPrologEngine(clauses)

      Left("permutation([1,2,3],L)") :: Right(new Struct("permutation", 1 to 3, new Var())) :: Nil foreach
        (struct => {
          val result = struct.fold(engine(_), engine(_))
          assert(result.size == 6)
          val permutations = result map (extractTerm(_, 1))
          permutations foreach (t => assert(t.isInstanceOf[Struct] && t.isList))
          permutations map (_.asInstanceOf[Struct].listIterator) map (Utility.iteratorToList(_)) foreach permutationAssert
        })
    }
  }
}
