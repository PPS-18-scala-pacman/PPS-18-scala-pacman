package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.model.Level.{Classic, pacmanSpeed, ghostSpeed, fruit, BASE_SPEED}
import it.unibo.scalapacman.lib.model.Fruit.{CHERRIES, KEY, BELL, GRAPES, GALAXIAN, APPLE, PEACH, STRAWBERRY}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec

class LevelTest extends AnyWordSpec with BeforeAndAfterAll {
  // scalastyle:off magic.number
  "Level" can {
    "calculate the pacman speed" in {
      assert(pacmanSpeed(1, SpeedCondition.NORM) == BASE_SPEED * .8)
      for (level <- 2 until 4) assert(pacmanSpeed(level, SpeedCondition.NORM) == BASE_SPEED * .9)
      for (level <- 5 until 20) assert(pacmanSpeed(level, SpeedCondition.NORM) == BASE_SPEED * 1)
      assert(pacmanSpeed(21, SpeedCondition.NORM) == BASE_SPEED * .9)

      // Freight
      assert(pacmanSpeed(1, SpeedCondition.FRIGHT) == BASE_SPEED * .9)
      for (level <- 2 until 4) assert(pacmanSpeed(level, SpeedCondition.FRIGHT) == BASE_SPEED * .95)
      for (level <- 5 until 20) assert(pacmanSpeed(level, SpeedCondition.FRIGHT) == BASE_SPEED)
      assert(pacmanSpeed(21, SpeedCondition.FRIGHT) == BASE_SPEED * .9)

      // Tunnel
      assert(pacmanSpeed(1, SpeedCondition.TUNNEL) == BASE_SPEED * .8)
      for (level <- 2 until 4) assert(pacmanSpeed(level, SpeedCondition.TUNNEL) == BASE_SPEED * .9)
      for (level <- 5 until 20) assert(pacmanSpeed(level, SpeedCondition.TUNNEL) == BASE_SPEED)
      assert(pacmanSpeed(21, SpeedCondition.TUNNEL) == BASE_SPEED * .9)
    }
    "calculate the ghost speed" in {
      assert(ghostSpeed(1, SpeedCondition.NORM) == BASE_SPEED * .75)
      for (level <- 2 until 4) assert(ghostSpeed(level, SpeedCondition.NORM) == BASE_SPEED * .85)
      for (level <- 5 until 21) assert(ghostSpeed(level, SpeedCondition.NORM) == BASE_SPEED * .95)

      // Fright
      assert(ghostSpeed(1, SpeedCondition.FRIGHT) == BASE_SPEED * .5)
      for (level <- 2 until 4) assert(ghostSpeed(level, SpeedCondition.FRIGHT) == BASE_SPEED * .55)
      for (level <- 5 until 20) assert(ghostSpeed(level, SpeedCondition.FRIGHT) == BASE_SPEED * .6)
      assert(ghostSpeed(21, SpeedCondition.FRIGHT) == BASE_SPEED * .95)

      // Tunnel
      assert(ghostSpeed(1, SpeedCondition.TUNNEL) == BASE_SPEED * .4)
      for (level <- 2 until 4) assert(ghostSpeed(level, SpeedCondition.TUNNEL) == BASE_SPEED * .45)
      for (level <- 5 until 21) assert(ghostSpeed(level, SpeedCondition.TUNNEL) == BASE_SPEED * .5)
    }
    "generate the correct fruit" in {
      assertResult(
        CHERRIES :: STRAWBERRY :: PEACH :: PEACH :: APPLE :: APPLE ::
        GRAPES :: GRAPES :: GALAXIAN :: GALAXIAN :: BELL :: BELL :: List.fill(8)(KEY)
      )(
        (for (level <- 1 until 21) yield fruit(level)) toList
      )
    }
    "generate the starting game state" in {
      assert(Classic(1).gameState == GameState(0))
    }
    "be generated using a generator" which {
      "create a classic game level" which {
        "contains all characters" in {
          val generator = Classic(1)
          assert(generator.characters.size == 5)
          generator.characters.foreach {
            case Pacman(pos, _, _, _) => assert(pos == generator.PACMAN_START_POSITION)
            case Ghost(GhostType.BLINKY, pos, _, _, _) => assert(pos == generator.BLINKY_START_POSITION)
            case Ghost(GhostType.CLYDE, pos, _, _, _) => assert(pos == generator.CLYDE_START_POSITION)
            case Ghost(GhostType.INKY, pos, _, _, _) => assert(pos == generator.INKY_START_POSITION)
            case Ghost(GhostType.PINKY, pos, _, _, _) => assert(pos == generator.PINKY_START_POSITION)
          }
        }
        "contains map" in {
          assert(Classic(1).map == Map.classic)
        }
        "can create game state" in {
          assert(Classic(1).gameState == GameState(0))
        }
        "can create fruit" in {
          assert(Classic(1).fruit == ((14, 17), CHERRIES))
        }
      }
    }
  }
}
