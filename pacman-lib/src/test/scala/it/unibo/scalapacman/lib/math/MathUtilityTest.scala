package it.unibo.scalapacman.lib.math

import org.scalatest.wordspec.AnyWordSpec
import MathUtility.{ round, roundAt }

class MathUtilityTest extends AnyWordSpec {
  val CUSTOM_ROUND_DIGITS = 7
  val INTEGER_NUMBER: Double = 3.0
  val FLOAT_WITH_TOO_MANY_DIGITS = 10.9874591237
  val FLOAT_ROUNDED_DEFAULT = 10.98746
  val FLOAT_ROUNDED_CUSTOM = 10.9874591
  val FLOAT_WITH_LOW_NUMBER_OF_DIGITS = 6.3

  "Math utilities" can {
    "round a number with custom digits after the comma" when {
      "the number is an integer" in {
        assertResult(INTEGER_NUMBER)(roundAt(CUSTOM_ROUND_DIGITS)(INTEGER_NUMBER))
      }
      "the number is zero" in {
        assertResult(0.0)(roundAt(CUSTOM_ROUND_DIGITS)(0.0))
      }
      "the number is a float to be rounded" in {
        assertResult(FLOAT_ROUNDED_CUSTOM)(roundAt(CUSTOM_ROUND_DIGITS)(FLOAT_WITH_TOO_MANY_DIGITS))
      }
      "the number is a float to not be rounded" in {
        assertResult(FLOAT_WITH_LOW_NUMBER_OF_DIGITS)(roundAt(CUSTOM_ROUND_DIGITS)(FLOAT_WITH_LOW_NUMBER_OF_DIGITS))
      }
    }
    "round a number with default digits after the comma" when {
      "the number is an integer" in {
        assertResult(INTEGER_NUMBER)(round(INTEGER_NUMBER))
      }
      "the number is zero" in {
        assertResult(0.0)(round(0.0))
      }
      "the number is a float to be rounded" in {
        assertResult(FLOAT_ROUNDED_DEFAULT)(round(FLOAT_WITH_TOO_MANY_DIGITS))
      }
      "the number is a float to not be rounded" in {
        assertResult(FLOAT_WITH_LOW_NUMBER_OF_DIGITS)(round(FLOAT_WITH_LOW_NUMBER_OF_DIGITS))
      }
    }
  }
}
