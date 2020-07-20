package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.Utility
import it.unibo.scalapacman.lib.model.Dot.{ENERGIZER_DOT, SMALL_DOT}
import it.unibo.scalapacman.lib.model.Tile.{GhostSpawn, Track, TrackSafe, TrackTunnel, Wall}

/**
 *
 * @param tiles A matrix of tiles
 */
case class Map(tiles: List[List[it.unibo.scalapacman.lib.model.Tile]])

case object Map {

  private def emptyTrack(f: Int = 0): Track = Track(None)

  private def smallDot(f: Int = 0): Track = Track(Some(SMALL_DOT))

  private def energizerDot(f: Int = 0): Track = Track(Some(ENERGIZER_DOT))

  private def wall(f: Int = 0): Wall = Wall()

  /**
   *
   * @return The classic map of Pacman (1980)
   */
  def classic: Map = Map(

    tiles = List[List[Tile]](
      // scalastyle:off
      List.tabulate(14)(wall),
      wall() :: List.tabulate(12)(smallDot) ::: wall() :: Nil,
      wall() :: smallDot() :: List.tabulate(4)(wall) ::: smallDot() :: List.tabulate(5)(wall) ::: smallDot() :: wall() :: Nil,
      wall() :: energizerDot() :: List.tabulate(4)(wall) ::: smallDot() :: List.tabulate(5)(wall) ::: smallDot() :: wall() :: Nil,
      wall() :: smallDot() :: List.tabulate(4)(wall) ::: smallDot() :: List.tabulate(5)(wall) ::: smallDot() :: wall() :: Nil,
      wall() :: List.tabulate(13)(smallDot),
      wall() :: smallDot() :: List.tabulate(4)(wall) ::: smallDot() :: wall() :: wall() :: smallDot() :: List.tabulate(4)(wall),
      wall() :: smallDot() :: List.tabulate(4)(wall) ::: smallDot() :: wall() :: wall() :: smallDot() :: List.tabulate(4)(wall),
      wall() :: List.tabulate(6)(smallDot) ::: wall() :: wall() :: List.tabulate(4)(smallDot) ::: wall() :: Nil,
      List.tabulate(6)(wall) ::: smallDot() :: List.tabulate(5)(wall) ::: emptyTrack() :: wall() :: Nil,
      List.tabulate(6)(wall) ::: smallDot() :: List.tabulate(5)(wall) ::: emptyTrack() :: wall() :: Nil,
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: emptyTrack() :: List.tabulate(3)(_ => TrackSafe()),
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: wall() :: wall() :: wall() :: GhostSpawn() :: Nil,
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: wall() :: List.tabulate(3)(_ => GhostSpawn()),
      List.tabulate(6)(_ => TrackTunnel()) ::: smallDot() :: List.tabulate(3)(emptyTrack) ::: wall() :: List.tabulate(3)(_ => GhostSpawn()),
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: wall() :: List.tabulate(3)(_ => GhostSpawn()),
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: List.tabulate(4)(wall),
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: emptyTrack() :: List.tabulate(3)(_ => TrackSafe()),
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: List.tabulate(4)(wall),
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: List.tabulate(4)(wall),
      wall() :: List.tabulate(12)(smallDot) ::: wall() :: Nil,
      wall() :: smallDot() :: List.tabulate(4)(wall) ::: smallDot() :: List.tabulate(5)(wall) ::: smallDot() :: wall() :: Nil,
      wall() :: smallDot() :: List.tabulate(4)(wall) ::: smallDot() :: List.tabulate(5)(wall) ::: smallDot() :: wall() :: Nil,
      wall() :: energizerDot() :: List.tabulate(2)(smallDot) ::: wall() :: wall() :: List.tabulate(7)(smallDot) ::: emptyTrack() :: Nil,
      List.tabulate(3)(wall) ::: smallDot() :: wall() :: wall() :: smallDot() :: wall() :: wall() :: smallDot() :: List.tabulate(4)(wall),
      List.tabulate(3)(wall) ::: smallDot() :: wall() :: wall() :: smallDot() :: wall() :: wall() :: smallDot() :: List.tabulate(4)(wall),
      wall() :: List.tabulate(6)(smallDot) ::: wall() :: wall() :: List.tabulate(4)(smallDot) ::: wall() :: Nil,
      wall() :: smallDot() :: List.tabulate(10)(wall) ::: smallDot() :: wall() :: Nil,
      wall() :: smallDot() :: List.tabulate(10)(wall) ::: smallDot() :: wall() :: Nil,
      wall() :: List.tabulate(13)(smallDot),
      List.tabulate(14)(wall)
      // scalastyle:on
    ).map(Utility.mirrorList)
  )
}

