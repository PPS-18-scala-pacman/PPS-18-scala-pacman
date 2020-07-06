package it.unibo.scalapacman.lib.model

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
    classicMap = Map.classic
  }

  "The map" which {
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
    }
  }
}
