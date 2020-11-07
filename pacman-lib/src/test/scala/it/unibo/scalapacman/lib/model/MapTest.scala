package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import it.unibo.scalapacman.lib.model.Direction.Direction
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec

class MapTest extends AnyWordSpec with BeforeAndAfterAll {
  val CLASSIC_HIGH = 31
  val CLASSIC_WIDE = 28
  val SMALL_DOTS_COUNT = 240
  val ENERGIZER_DOTS_COUNT = 4
  val TUNNEL_TRACKS_COUNT = 12
  val SAFE_TRACKS_COUNT = 12
  val SPAWN_TRACKS_COUNT = 20
  var classicMap: Map = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    classicMap = Map.create(MapType.CLASSIC)
  }

  "The map" when {
    "classic, from Pacman first edition," should {
      "be equally high" in {
        assertResult(CLASSIC_HIGH)(classicMap.tiles.size)
      }
      "be equally wide" in {
        for (row <- classicMap.tiles) yield assertResult(CLASSIC_WIDE)(row.size)
      }
      "contain the same number of small dots" in {
        assertResult(SMALL_DOTS_COUNT)(classicMap.tiles.flatten.count(tile => tile.eatable.contains(Dot.SMALL_DOT)))
      }
      "contain the same number of energizer dots" in {
        assertResult(ENERGIZER_DOTS_COUNT)(classicMap.tiles.flatten.count(tile => tile.eatable.contains(Dot.ENERGIZER_DOT)))
      }
      "contain the same number of tunnel tracks" in {
        assertResult(TUNNEL_TRACKS_COUNT)(classicMap.tiles.flatten.count(tile => tile.isInstanceOf[Tile.TrackTunnel]))
      }
      "contain the same number of safe tracks" in {
        assertResult(SAFE_TRACKS_COUNT)(classicMap.tiles.flatten.count(tile => tile.isInstanceOf[Tile.TrackSafe]))
      }
      "contain the same number of ghost spawn tracks" in {
        assertResult(SPAWN_TRACKS_COUNT)(classicMap.tiles.flatten.count(tile => tile.isInstanceOf[Tile.GhostSpawn]))
      }
      "return the correct starting position for every character" in {
        assert(Map.getStartPosition(MapType.CLASSIC, Pacman, PacmanType.PACMAN) == Map.Classic.PACMAN_START_POSITION)
        assert(Map.getStartPosition(MapType.CLASSIC, Pacman, PacmanType.MS_PACMAN) != Map.Classic.PACMAN_START_POSITION)
        assert(Map.getStartPosition(MapType.CLASSIC, Ghost, GhostType.BLINKY) == Map.Classic.BLINKY_START_POSITION)
        assert(Map.getStartPosition(MapType.CLASSIC, Ghost, GhostType.PINKY) == Map.Classic.PINKY_START_POSITION)
        assert(Map.getStartPosition(MapType.CLASSIC, Ghost, GhostType.INKY) == Map.Classic.INKY_START_POSITION)
        assert(Map.getStartPosition(MapType.CLASSIC, Ghost, GhostType.CLYDE) == Map.Classic.CLYDE_START_POSITION)
        assertThrows[java.lang.IllegalArgumentException](Map.getStartPosition(MapType.CLASSIC, new Character {
          override val characterType: CharacterType = new CharacterType {}
          override val position: Point2D = Point2D(0, 0)
          override val speed: Double = 1.0
          override val direction: Direction = Direction.NORTH
          override val isDead: Boolean = false
        }, new CharacterType {}))
      }
      "return the correct respawn position for every character" in {
        assert(Map.getRestartPosition(MapType.CLASSIC, Pacman, PacmanType.PACMAN) == Map.Classic.PACMAN_START_POSITION)
        assert(Map.getRestartPosition(MapType.CLASSIC, Pacman, PacmanType.MS_PACMAN) != Map.Classic.PACMAN_START_POSITION)
        assert(Map.getRestartPosition(MapType.CLASSIC, Ghost, GhostType.BLINKY) == Map.Classic.PINKY_START_POSITION)
        assert(Map.getRestartPosition(MapType.CLASSIC, Ghost, GhostType.PINKY) == Map.Classic.PINKY_START_POSITION)
        assert(Map.getRestartPosition(MapType.CLASSIC, Ghost, GhostType.INKY) == Map.Classic.INKY_START_POSITION)
        assert(Map.getRestartPosition(MapType.CLASSIC, Ghost, GhostType.CLYDE) == Map.Classic.CLYDE_START_POSITION)
        assertThrows[java.lang.IllegalArgumentException](Map.getRestartPosition(MapType.CLASSIC, new Character {
          override val characterType: CharacterType = new CharacterType {}
          override val position: Point2D = Point2D(0, 0)
          override val speed: Double = 1.0
          override val direction: Direction = Direction.NORTH
          override val isDead: Boolean = false
        }, new CharacterType {}))
      }
      "return the correct spawn map indexes for fruits" in {
        assert(Map.getFruitMapIndexes(MapType.CLASSIC) == (14, 17))
      }
    }
  }
}
