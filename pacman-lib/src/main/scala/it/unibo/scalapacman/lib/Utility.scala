package it.unibo.scalapacman.lib

object Utility {
  def mirrorList[A](list: List[A]): List[A] = list ::: list.reverse
}
