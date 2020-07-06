package it.unibo.scalapacman.lib.math

import scala.math.pow

object MathUtility {
  private val BASE_TEN = 10

  /**
   * Round at custom numbers after the comma
   * @param p numbers after the comma
   * @param n number to be rounded
   * @return the rounded number
   */
  def roundAt(p: Int)(n: Double): Double = {
    val s = pow(BASE_TEN, p)
    (n * s).round / s
  }

  private val DEFAULT_PRECISION: Int = 5

  /**
   * Round with five numbers after the comma
   * @return the rounded number
   */
  def round: Double => Double = roundAt(DEFAULT_PRECISION)
}
