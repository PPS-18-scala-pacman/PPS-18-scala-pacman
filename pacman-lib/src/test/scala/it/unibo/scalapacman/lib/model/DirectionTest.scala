package it.unibo.scalapacman.lib.model

import org.scalatest.wordspec.AnyWordSpec

class DirectionTest extends AnyWordSpec {

  "A Direction" should {
    "be calculated from a starting tile to a neighboring tile" in {
      assert(Direction.byPath(((0, 0), (1, 0))) == Direction.EAST)
      assert(Direction.byPath(((0, 0), (0, 1))) == Direction.SOUTH)
      assert(Direction.byPath(((0, 0), (-1, 0))) == Direction.WEST)
      assert(Direction.byPath(((0, 0), (0, -1))) == Direction.NORTH)
    }
  }
}
