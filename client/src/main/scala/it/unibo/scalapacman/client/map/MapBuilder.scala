package it.unibo.scalapacman.client.map

import it.unibo.scalapacman.lib.model.Dot.{ENERGIZER_DOT, SMALL_DOT}
import it.unibo.scalapacman.lib.model.Map
import it.unibo.scalapacman.lib.model.Tile.{GhostSpawn, Track, TrackSafe, Wall}

object MapBuilder {

  val mapClassic: Map = Map.classic

  def buildClassic(): List[List[Char]] = build(mapClassic)

  private def build(map: Map): List[List[Char]] = map.tiles map (row => row map {
    case GhostSpawn() | TrackSafe() | Track(None) => ElementsCharCode.EMPTY_SPACE_CODE
    case Track(Some(SMALL_DOT)) => ElementsCharCode.DOT_CODE
    case Track(Some(ENERGIZER_DOT)) => ElementsCharCode.ENERGIZED_DOT_CODE
    case Wall() => ElementsCharCode.WALL_CODE
    case _ => ElementsCharCode.EMPTY_SPACE_CODE
  })
}
