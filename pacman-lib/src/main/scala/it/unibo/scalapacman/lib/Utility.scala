package it.unibo.scalapacman.lib

import it.unibo.scalapacman.lib.math.Point2D

object Utility {
  def positionToTileNumber(position: Point2D, tileSize: Int): (Int, Int) = {
    def valueToTileNumber(x: Double): Int = (x / tileSize).toInt
    (
      valueToTileNumber(position.x),
      valueToTileNumber(position.y)
    )
  }

  def mirrorList[A](list: List[A]): List[A] = list ::: list.reverse
}
