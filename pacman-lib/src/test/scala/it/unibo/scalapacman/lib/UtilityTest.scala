package it.unibo.scalapacman.lib

import org.scalatest.wordspec.AnyWordSpec

class UtilityTest extends AnyWordSpec {
  "Utility" should {
    "read a file" in {
      assert(Utility.readFile(getClass.getResource("/readFile.txt")) == "readFile works!")
    }
  }
}
