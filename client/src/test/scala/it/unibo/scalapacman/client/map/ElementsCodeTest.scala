package it.unibo.scalapacman.client.map

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ElementsCodeTest extends AnyWordSpec with Matchers {

  val elementsCodes: List[String] = ElementsCode.PACMAN_CODES_MAP.values.toList ::: ElementsCode.GHOST_CODES_MAP.values.toList :::
    ElementsCode.DOT_CODES_MAP.values.toList ::: ElementsCode.FRUIT_CODES_MAP.values.toList :::
    ElementsCode.ARROW_CODES_MAP.values.toList ::: ElementsCode.EMPTY_SPACE_CODE :: ElementsCode.WALL_CODE :: Nil

  "ElementsCode" should {
    "return all codes" in {
      ElementsCode.codes shouldEqual elementsCodes
    }

    "recognize pacman codes" in {
      ElementsCode.PACMAN_CODES_MAP.values.toList foreach { ElementsCode.matchPacman(_) shouldEqual true }

      ElementsCode.codes filter (!ElementsCode.PACMAN_CODES_MAP.values.toList.contains(_)) foreach {
        ElementsCode.matchPacman(_) shouldEqual false
      }
    }

    "recognize ghosts codes" in {
      ElementsCode.GHOST_CODES_MAP.values.toList foreach { ElementsCode.matchGhost(_) shouldEqual true }

      ElementsCode.codes filter (!ElementsCode.GHOST_CODES_MAP.values.toList.contains(_)) foreach {
        ElementsCode.matchGhost(_) shouldEqual false
      }
    }

    "recognize dot codes" in {
      ElementsCode.DOT_CODES_MAP.values.toList foreach { ElementsCode.matchDot(_) shouldEqual true }

      ElementsCode.codes filter (!ElementsCode.DOT_CODES_MAP.values.toList.contains(_)) foreach {
        ElementsCode.matchDot(_) shouldEqual false
      }
    }

    "recognize fruit codes" in {
      ElementsCode.FRUIT_CODES_MAP.values.toList foreach { ElementsCode.matchFruit(_) shouldEqual true }

      ElementsCode.codes filter (!ElementsCode.FRUIT_CODES_MAP.values.toList.contains(_)) foreach {
        ElementsCode.matchFruit(_) shouldEqual false
      }
    }
  }
}
