package it.unibo.scalapacman.common

import it.unibo.scalapacman.lib.model.GhostType
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameCharacterTest extends AnyWordSpec with BeforeAndAfterAll with Matchers {

  "GameCharacter" should {
    "return GameCharacter.Value from GhostType" in {
      GameCharacter.ghostTypeToGameCharacter(GhostType.BLINKY) shouldEqual GameCharacter.BLINKY
      GameCharacter.ghostTypeToGameCharacter(GhostType.PINKY) shouldEqual GameCharacter.PINKY
      GameCharacter.ghostTypeToGameCharacter(GhostType.INKY) shouldEqual GameCharacter.INKY
      GameCharacter.ghostTypeToGameCharacter(GhostType.CLYDE) shouldEqual GameCharacter.CLYDE
    }
  }
}
