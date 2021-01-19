package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import it.unibo.scalapacman.lib.model.Level.{BASE_SPEED, Classic, fruit, ghostSpeed, pacmanSpeed, energizerDuration, ghostRespawnDotCounter}
import it.unibo.scalapacman.lib.model.Fruit.{APPLE, BELL, CHERRIES, GALAXIAN, GRAPES, KEY, PEACH, STRAWBERRY}
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
    "generate the energizer duration" in {
      val durations = (for (level <- 1 until 21) yield energizerDuration(level)).toList
      assert(durations == List(6000, 5000, 4000, 3000, 2000, 5000, 2000, 2000, 1000, 5000, 2000, 1000, 1000, 3000, 1000, 1000, 0, 1000, 0, 0))
    }
    "generate the dot counter to respawn a ghost" in {
      assert(ghostRespawnDotCounter(1, GhostType.INKY) == 7)
      assert(ghostRespawnDotCounter(1, GhostType.CLYDE) == 17)
      assert(ghostRespawnDotCounter(1, GhostType.BLINKY) == 0)
      assert(ghostRespawnDotCounter(1, GhostType.PINKY) == 0)
    }
    "generate the starting game state" in {
      assert(Classic(1).gameState == GameState(0))
    }
    "generate the starting game events" in {
      assert(
        Classic(1).gameEvents ==
          GameTimedEvent(GameTimedEventType.FRUIT_SPAWN, dots = Some(174), payload = Some(CHERRIES)) ::
            GameTimedEvent(GameTimedEventType.FRUIT_SPAWN, dots = Some(74), payload = Some(CHERRIES)) ::
            GameTimedEvent(GameTimedEventType.GHOST_RESTART, dots = Some(214), payload = Some(GhostType.INKY)) ::
            GameTimedEvent(GameTimedEventType.GHOST_RESTART, dots = Some(184), payload = Some(GhostType.CLYDE)) ::
            Nil
      )
    }
    "be generated using a generator" which {
      "create a classic game level" which {
        "contains all characters with one player" in {
          val generator = Classic(1)
          val numPlayers = 1
          assert(generator.characters(numPlayers).size == 4 + numPlayers)
          generator.characters(numPlayers).foreach {
            case Pacman(PacmanType.PACMAN, pos, _, _, _) => assert(pos == Map.Classic.PACMAN_START_POSITION)
            case Ghost(GhostType.BLINKY, pos, _, _, _) => assert(pos == Map.Classic.BLINKY_START_POSITION)
            case Ghost(GhostType.CLYDE, pos, _, _, _) => assert(pos == Map.Classic.CLYDE_START_POSITION)
            case Ghost(GhostType.INKY, pos, _, _, _) => assert(pos == Map.Classic.INKY_START_POSITION)
            case Ghost(GhostType.PINKY, pos, _, _, _) => assert(pos == Map.Classic.PINKY_START_POSITION)
          }
        }
        "contains all characters with one multiple players" in {
          val generator = Classic(1)
          val numPlayers = 4
          assert(generator.characters(numPlayers).size == 4 + numPlayers)
          val pacmans: List[Pacman] = generator.characters(numPlayers)
            .collect({ case p: Pacman => p })
          assert(pacmans.exists(_.characterType == PacmanType.PACMAN))
          assert(pacmans.exists(_.characterType == PacmanType.MS_PACMAN))
          assert(pacmans.exists(_.characterType == PacmanType.CAPMAN))
          assert(pacmans.exists(_.characterType == PacmanType.RAPMAN))
        }
        "contains map" in {
          assert(Classic(1).map == Map.create(MapType.CLASSIC))
        }
        "can create game state" in {
          assert(Classic(1).gameState == GameState(0))
        }
        "can create fruit" in {
          assert(Classic(1).fruit == CHERRIES)
        }
      }
    }
  }
}
