package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.Utility
import it.unibo.scalapacman.lib.model.Dot.{ENERGIZER_DOT, SMALL_DOT}
import it.unibo.scalapacman.lib.model.Tile.{GhostSpawn, Tile, Track, TrackSafe, TrackTunnel, Wall}

/**
 *
 * @param tiles A matrix of tiles
 */
case class Map(tiles: List[List[Tile]])


case object Map {

  /**
   *
   * @return The classic map of Pacman (1980)
   */
  def classic: Map = Map(

    tiles = List[List[Tile]](
      // scalastyle:off
      List.fill(14)(Wall()),
      Wall() :: List.fill(12)(Track(Some(SMALL_DOT))) ::: Wall() :: Nil,
      Wall() :: Track(Some(SMALL_DOT)) :: List.fill(4)(Wall()) ::: Track(Some(SMALL_DOT)) :: List.fill(5)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Nil,
      Wall() :: Track(Some(ENERGIZER_DOT)) :: List.fill(4)(Wall()) ::: Track(Some(SMALL_DOT)) :: List.fill(5)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Nil,
      Wall() :: Track(Some(SMALL_DOT)) :: List.fill(4)(Wall()) ::: Track(Some(SMALL_DOT)) :: List.fill(5)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Nil,
      Wall() :: List.fill(13)(Track(Some(SMALL_DOT))),
      Wall() :: Track(Some(SMALL_DOT)) :: List.fill(4)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(Some(SMALL_DOT)) :: List.fill(4)(Wall()),
      Wall() :: Track(Some(SMALL_DOT)) :: List.fill(4)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(Some(SMALL_DOT)) :: List.fill(4)(Wall()),
      Wall() :: List.fill(6)(Track(Some(SMALL_DOT))) ::: Wall() :: Wall() :: List.fill(4)(Track(Some(SMALL_DOT))) ::: Wall() :: Nil,
      List.fill(6)(Wall()) ::: Track(Some(SMALL_DOT)) :: List.fill(5)(Wall()) ::: Track(None) :: Wall() :: Nil,
      List.fill(6)(Wall()) ::: Track(Some(SMALL_DOT)) :: List.fill(5)(Wall()) ::: Track(None) :: Wall() :: Nil,
      List.fill(6)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(None) :: Track(None) :: List.fill(3)(TrackSafe()),
      List.fill(6)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(None) :: Wall() :: Wall() :: Wall() :: GhostSpawn() :: Nil,
      List.fill(6)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(None) :: Wall() :: List.fill(3)(GhostSpawn()),
      List.fill(6)(TrackTunnel()) ::: Track(Some(SMALL_DOT)) :: List.fill(3)(Track(None)) ::: Wall() :: List.fill(3)(GhostSpawn()),
      List.fill(6)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(None) :: Wall() :: List.fill(3)(GhostSpawn()),
      List.fill(6)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(None) :: List.fill(4)(Wall()),
      List.fill(6)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(None) :: Track(None) :: List.fill(3)(TrackSafe()),
      List.fill(6)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(None) :: List.fill(4)(Wall()),
      List.fill(6)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(None) :: List.fill(4)(Wall()),
      Wall() :: List.fill(12)(Track(Some(SMALL_DOT))) ::: Wall() :: Nil,
      Wall() :: Track(Some(SMALL_DOT)) :: List.fill(4)(Wall()) ::: Track(Some(SMALL_DOT)) :: List.fill(5)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Nil,
      Wall() :: Track(Some(SMALL_DOT)) :: List.fill(4)(Wall()) ::: Track(Some(SMALL_DOT)) :: List.fill(5)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Nil,
      Wall() :: Track(Some(ENERGIZER_DOT)) :: List.fill(2)(Track(Some(SMALL_DOT))) ::: Wall() :: Wall() :: List.fill(7)(Track(Some(SMALL_DOT))) ::: Track(None) :: Nil,
      List.fill(3)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(Some(SMALL_DOT)) :: List.fill(4)(Wall()),
      List.fill(3)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(Some(SMALL_DOT)) :: Wall() :: Wall() :: Track(Some(SMALL_DOT)) :: List.fill(4)(Wall()),
      Wall() :: List.fill(6)(Track(Some(SMALL_DOT))) ::: Wall() :: Wall() :: List.fill(4)(Track(Some(SMALL_DOT))) ::: Wall() :: Nil,
      Wall() :: Track(Some(SMALL_DOT)) :: List.fill(10)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Nil,
      Wall() :: Track(Some(SMALL_DOT)) :: List.fill(10)(Wall()) ::: Track(Some(SMALL_DOT)) :: Wall() :: Nil,
      Wall() :: List.fill(13)(Track(Some(SMALL_DOT))),
      List.fill(14)(Wall())
      // scalastyle:on
    ).map(Utility.mirrorList)
  )
}

