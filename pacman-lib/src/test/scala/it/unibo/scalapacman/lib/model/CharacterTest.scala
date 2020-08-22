package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import org.scalatest.wordspec.AnyWordSpec

class CharacterTest extends AnyWordSpec {
  "A Character" should {
    "be copied" in {
      val pacman = Pacman(Point2D(0, 0), 1.0, Direction.EAST)
      assert(Character.copy(pacman)(position = Point2D(1, 1)) == pacman.copy(position = Point2D(1, 1)))

      val blinky = Ghost(GhostType.BLINKY, Point2D(0, 0), 1.0, Direction.EAST)
      assert(Character.copy(blinky)(speed = 2.0) == blinky.copy(speed = 2.0))

      val pinky = Ghost(GhostType.PINKY, Point2D(0, 0), 1.0, Direction.EAST)
      assert(Character.copy(pinky)(direction = Direction.NORTH) == pinky.copy(direction = Direction.NORTH))

      val inky = Ghost(GhostType.INKY, Point2D(0, 0), 1.0, Direction.EAST, isDead = true)
      assert(Character.copy(inky)(isDead = false) == inky.copy(isDead = false))

      val clyde = Ghost(GhostType.CLYDE, Point2D(0, 0), 1.0, Direction.EAST)
      assert(Character.copy(clyde)(position = Point2D(1, 1)) == clyde.copy(position = Point2D(1, 1)))
    }
  }
}
