package it.unibo.scalapacman.client.map

import it.unibo.scalapacman.client.map.PacmanMap.PacmanMap
import it.unibo.scalapacman.lib.model.Dot.{ENERGIZER_DOT, SMALL_DOT}
import it.unibo.scalapacman.lib.model.Map
import it.unibo.scalapacman.lib.model.Tile.{GhostSpawn, Track, TrackSafe, Wall}

object MapBuilder {

  val mapClassic: Map = Map.classic

  def buildClassic(): PacmanMap = build(mapClassic)

  private def build(map: Map): PacmanMap = map.tiles map (row => row map {
    case Wall() => ElementsCode.WALL_CODE
    case GhostSpawn() | TrackSafe() | Track(None) | Track(Some(SMALL_DOT)) | Track(Some(ENERGIZER_DOT)) => ElementsCode.EMPTY_SPACE_CODE
    case _ => ElementsCode.EMPTY_SPACE_CODE
  })
}
