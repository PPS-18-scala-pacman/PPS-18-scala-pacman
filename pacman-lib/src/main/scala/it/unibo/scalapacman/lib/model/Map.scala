package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.Utility
import it.unibo.scalapacman.lib.math.{Point2D, TileGeography}
import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import it.unibo.scalapacman.lib.model.Dot.{ENERGIZER_DOT, SMALL_DOT}
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.lib.model.Tile.{GhostSpawn, Track, TrackSafe, TrackTunnel, Wall}

import scala.reflect.ClassTag

object MapType extends Enumeration {
  type MapType = Value
  val CLASSIC, CUSTOM = Value
}

/**
 *
 * @param tiles A matrix of tiles
 */
case class Map(
                tiles: List[List[Tile]],
                mapType: MapType.MapType = MapType.CUSTOM)

case object Map {
  type MapIndexes = (Int, Int)

  def emptyTrack(f: Int = 0): Track = Track(None)

  def smallDot(f: Int = 0): Track = Track(Some(SMALL_DOT))

  def energizerDot(f: Int = 0): Track = Track(Some(ENERGIZER_DOT))

  def trackSafe(f: Int = 0): TrackSafe = TrackSafe(None)

  def wall(f: Int = 0): Wall = Wall()

  object Classic {
    val tiles: List[List[Tile]] = List[List[Tile]](
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
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: emptyTrack() :: List.tabulate(3)(trackSafe),
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: wall() :: wall() :: wall() :: GhostSpawn() :: Nil,
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: wall() :: List.tabulate(3)(_ => GhostSpawn()),
      List.tabulate(6)(_ => TrackTunnel()) ::: smallDot() :: List.tabulate(3)(emptyTrack) ::: wall() :: List.tabulate(3)(_ => GhostSpawn()),
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: wall() :: List.tabulate(3)(_ => GhostSpawn()),
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: List.tabulate(4)(wall),
      List.tabulate(6)(wall) ::: smallDot() :: wall() :: wall() :: emptyTrack() :: emptyTrack() :: List.tabulate(3)(trackSafe),
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

    val PACMAN_START_POSITION: Point2D = Point2D(14 * TileGeography.SIZE, 23 * TileGeography.SIZE) + TileGeography.center
    val BLINKY_START_POSITION: Point2D = Point2D(13 * TileGeography.SIZE, 11 * TileGeography.SIZE) + TileGeography.center
    val PINKY_START_POSITION: Point2D = Point2D(11 * TileGeography.SIZE, 14 * TileGeography.SIZE) + TileGeography.center
    val INKY_START_POSITION: Point2D = Point2D(13 * TileGeography.SIZE, 14 * TileGeography.SIZE) + TileGeography.center
    val CLYDE_START_POSITION: Point2D = Point2D(15 * TileGeography.SIZE, 14 * TileGeography.SIZE) + TileGeography.center
    val FRUIT_INDEXES: MapIndexes = (14, 17)

    def getStartPosition[T >: Character: ClassTag](c: T, ghostType: Option[GhostType] = None): Point2D = (c, ghostType) match {
      case (Pacman, _) => PACMAN_START_POSITION
      case (Ghost, Some(GhostType.BLINKY)) => BLINKY_START_POSITION
      case (Ghost, Some(GhostType.PINKY)) => PINKY_START_POSITION
      case (Ghost, Some(GhostType.INKY)) => INKY_START_POSITION
      case (Ghost, Some(GhostType.CLYDE)) => CLYDE_START_POSITION
      case _ => throw new IllegalArgumentException("Unknown character")
    }

    def getRestartPosition[T >: Character: ClassTag](c: T, ghostType: Option[GhostType] = None): Point2D = (c, ghostType) match {
      case (Ghost, Some(GhostType.BLINKY)) => PINKY_START_POSITION
      case _ => getStartPosition(c, ghostType)
    }

    def getFruitMapIndexes: MapIndexes = FRUIT_INDEXES
  }

  def getStartPosition[T >: Character: ClassTag](mapType: MapType.MapType, c: T, ghostType: Option[GhostType] = None): Point2D = mapType match {
    case MapType.CLASSIC => Classic.getStartPosition(c, ghostType)
  }

  def getRestartPosition[T >: Character: ClassTag](mapType: MapType.MapType, c: T, ghostType: Option[GhostType] = None): Point2D = mapType match {
    case MapType.CLASSIC => Classic.getRestartPosition(c, ghostType)
  }

  def getFruitMapIndexes(mapType: MapType.MapType): MapIndexes = mapType match {
    case MapType.CLASSIC => Classic.getFruitMapIndexes
  }

  /**
   * Crea un'implementazione del tipo di mappa richiesto.
   * @return La mappa richiesta
   */
  def create(mapType: MapType.MapType = MapType.CUSTOM): Map = Map(
    tiles = mapType match { case MapType.CLASSIC => Classic.tiles },
    mapType = mapType
  )
}

