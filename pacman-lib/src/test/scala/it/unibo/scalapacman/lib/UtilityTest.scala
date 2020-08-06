package it.unibo.scalapacman.lib

import it.unibo.scalapacman.lib.model.Direction
import org.scalatest.wordspec.AnyWordSpec

class UtilityTest extends AnyWordSpec {
  "Utility" should {
    "read a file" in {
      assert(Utility.readFile(getClass.getResource("/readFile.txt")) == "readFile works!")
    }
    "create a mirror list" in {
      assert(Utility.mirrorList(0 :: 1 :: 2 :: Nil) == 0 :: 1 :: 2 :: 2 :: 1 :: 0 :: Nil)
      assert(Utility.mirrorList(List()) == List())
    }
    "calculate the direction from a starting tile to a neighboring tile" in {
      assert(Utility.directionByPath((0, 0) :: (1, 0) :: Nil) == Direction.EAST)
      assert(Utility.directionByPath((0, 0) :: (0, 1) :: Nil) == Direction.SOUTH)
      assert(Utility.directionByPath((0, 0) :: (-1, 0) :: Nil) == Direction.WEST)
      assert(Utility.directionByPath((0, 0) :: (0, -1) :: Nil) == Direction.NORTH)
    }
  }
}
