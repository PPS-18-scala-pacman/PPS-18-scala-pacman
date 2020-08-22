package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.engine.GameMovement.move
import it.unibo.scalapacman.lib.math.{Point2D, TileGeography}
import it.unibo.scalapacman.lib.model.Character.Pacman
import it.unibo.scalapacman.lib.model.{Character, Direction, Map, Tile}
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

class GameMovementTest extends AnyWordSpec {
  val MAP_SIZE = 4
  val TIME_MS = 1
  implicit val MAP: Map = Map(
    List.fill(MAP_SIZE - 1)(
      List.fill(MAP_SIZE)(Tile.Track(None))
    ) :::
      List(List.fill(MAP_SIZE)(Tile.Wall()))
  )

  "Moving in a map, a character" when {
    "it desires to curve" should {
      "curve if it's possible" in {
        val character = Pacman(
          TileGeography.eastGate,
          TileGeography.SIZE,
          Direction.WEST
        )
        val desiredDirection = Direction.SOUTH
        val attended: Point2D = TileGeography.southGate
        val result: Character = move(character, TIME_MS, desiredDirection)
        assert(result.position.x == attended.x && result.position.y == attended.y && result.direction == desiredDirection)
      }
      "go straight if it isn't possible" in {
        val character = Pacman(
          TileGeography.eastGate + Point2D(0, TileGeography.SIZE * 2),
          TileGeography.SIZE,
          Direction.WEST
        )
        val attended: Point2D = TileGeography.westGate + Point2D(0, TileGeography.SIZE * 2)
        val result: Character = move(character, TIME_MS, Direction.SOUTH)
        assert(result.position.x == attended.x && result.position.y == attended.y && result.direction == character.direction)
      }
    }
    "it desires to go straight" should {
      "go straight if it's possible" in {
        val character = Pacman(
          TileGeography.center,
          TileGeography.SIZE,
          Direction.WEST
        )
        val attended: Point2D = TileGeography.center + Point2D(TileGeography.SIZE * 3, 0)
        val result: Character = move(character, TIME_MS, character.direction)
        assert(result.position.x == attended.x && result.position.y == attended.y && result.direction == character.direction)
      }
      "go straight the most if it's not possible to reach the next center" in {
        val character = Pacman(
          TileGeography.center,
          TileGeography.SIZE / 2,
          Direction.EAST
        )
        val attended: Point2D = TileGeography.center + Point2D(TileGeography.SIZE * 0.5, 0)
        val result: Character = move(character, TIME_MS, character.direction)
        assert(result.position.x == attended.x && result.position.y == attended.y && result.direction == character.direction)
      }
      "stop if it isn't possible" in {
        val character = Pacman(
          TileGeography.northGate + Point2D(0, TileGeography.SIZE * 2),
          TileGeography.SIZE,
          Direction.SOUTH
        )
        val attended: Point2D = TileGeography.center + Point2D(0, TileGeography.SIZE * 2)
        val result: Character = move(character, TIME_MS, character.direction)
        assert(result.position.x == attended.x && result.position.y == attended.y && result.direction == character.direction)
      }
    }
    "it desires to revert direction" should {
      "revert it and go straight if it's possible" in {
        val character = Pacman(
          TileGeography.center,
          TileGeography.SIZE,
          Direction.NORTH
        )
        val desiredDirection = Direction.SOUTH
        val attended: Point2D = TileGeography.center + Point2D(0, TileGeography.SIZE)
        val result: Character = move(character, TIME_MS, desiredDirection)
        assert(result.position.x == attended.x && result.position.y == attended.y && result.direction == desiredDirection)
      }
      "revert it and stop if it isn't possible" in {
        val character = Pacman(
          TileGeography.center,
          TileGeography.SIZE,
          Direction.SOUTH
        )
        val desiredDirection = Direction.NORTH
        val attended: Point2D = character.position
        val result: Character = move(character, TIME_MS, desiredDirection)
        assert(result.position.x == attended.x && result.position.y == attended.y && result.direction == desiredDirection)
      }
    }
    "it want to move" should {
      "be able to use FiniteDuration" in {
        val character = Pacman(
          TileGeography.center,
          TileGeography.SIZE,
          Direction.WEST
        )
        val attended: Point2D = TileGeography.center + Point2D(TileGeography.SIZE * 3, 0)
        val result: Character = GameMovement.move(character, FiniteDuration(1, MILLISECONDS), Some(character.direction))
        assert(result == move(character, TIME_MS, character.direction))
      }
      "be able to don't have a desired direction" in {
        val character = Pacman(
          TileGeography.center,
          TileGeography.SIZE,
          Direction.WEST
        )
        val attended: Point2D = TileGeography.center + Point2D(TileGeography.SIZE * 3, 0)
        val result: Character = GameMovement.move(character, FiniteDuration(1, MILLISECONDS), None)
        assert(result == move(character, TIME_MS, character.direction))
      }
      "not move" when {
        "it is died" in {
          val character = Pacman(
            TileGeography.center,
            TileGeography.SIZE,
            Direction.WEST,
            isDead = true
          )
          val result: Character = move(character, TIME_MS, character.direction)
          assert(result.position == character.position)
        }
        "it has no time" in {
          val character = Pacman(
            TileGeography.center,
            TileGeography.SIZE,
            Direction.WEST
          )
          val result: Character = move(character, timeMs = 0, character.direction)
          assert(result.position == character.position)
        }
      }
    }
  }
}
