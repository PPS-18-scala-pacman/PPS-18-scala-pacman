package it.unibo.scalapacman.server.model

import it.unibo.scalapacman.lib.model.Direction.{Direction, EAST, NORTH, NORTHEAST, NORTHWEST, SOUTH, SOUTHEAST, SOUTHWEST, WEST}

object MoveDirection extends Enumeration {
  type MoveDirection = Value
  val UP, DOWN, RIGHT, LEFT = Value

  implicit def moveDirectionToDirection(move: MoveDirection): Direction = move match {
    case UP     => NORTH
    case DOWN   => SOUTH
    case LEFT   => WEST
    case RIGHT  => EAST
  }

  implicit def directionToMoveDirection(dir: Direction): MoveDirection = dir match {
    case NORTH | NORTHEAST | NORTHWEST  => UP
    case SOUTH | SOUTHEAST | SOUTHWEST  => DOWN
    case EAST                           => RIGHT
    case WEST                           => LEFT
  }
}
