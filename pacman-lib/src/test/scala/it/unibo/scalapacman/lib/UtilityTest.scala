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
  }
}
