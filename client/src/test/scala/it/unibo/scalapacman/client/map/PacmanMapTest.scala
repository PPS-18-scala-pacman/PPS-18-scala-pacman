package it.unibo.scalapacman.client.map

import org.scalatest.wordspec.AnyWordSpecLike
import it.unibo.scalapacman.client.map.ElementsCode.{CHERRIES_CODE, DOT_CODE, EMPTY_SPACE_CODE, ENERGIZER_DOT_CODE, GALAXIAN_CODE, WALL_CODE}
import it.unibo.scalapacman.client.map.PacmanMap.{BLINKY_STYLE, CLYDE_STYLE, INKY_STYLE, PACMAN_STYLE, PINKY_STYLE, PacmanMap}
import it.unibo.scalapacman.common.{DirectionHolder, GameCharacter, GameCharacterHolder, GameEntityDTO}
import it.unibo.scalapacman.lib.model.{Direction, Fruit, Map, MapType}
import it.unibo.scalapacman.lib.Utility
import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.Tile.{Track, TrackSafe}
import org.scalatest.matchers.should.Matchers

class PacmanMapTest
  extends AnyWordSpecLike
    with Matchers {

  private val WALL = (WALL_CODE, Some(PacmanMap.WALL_STYLE))
  private val DOT = (DOT_CODE, None)
  private val ENERGIZER_DOT = (ENERGIZER_DOT_CODE, None)
  private val EMPTY_SPACE = (EMPTY_SPACE_CODE, None)

  // scalastyle:off
  val mapClassicBuilt: PacmanMap = List(
    List.tabulate(14)(_ => WALL),
    WALL :: List.tabulate(12)(_ => DOT) ::: WALL :: Nil,
    WALL :: DOT :: List.tabulate(4)(_ => WALL) ::: DOT :: List.tabulate(5)(_ => WALL) ::: DOT :: WALL :: Nil,
    WALL :: ENERGIZER_DOT :: List.tabulate(4)(_ => WALL) ::: DOT :: List.tabulate(5)(_ => WALL) ::: DOT :: WALL :: Nil,
    WALL :: DOT :: List.tabulate(4)(_ => WALL) ::: DOT :: List.tabulate(5)(_ => WALL) ::: DOT :: WALL :: Nil,
    WALL :: List.tabulate(13)(_ => DOT),
    WALL :: DOT :: List.tabulate(4)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: DOT :: List.tabulate(4)(_ => WALL),
    WALL :: DOT :: List.tabulate(4)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: DOT :: List.tabulate(4)(_ => WALL),
    WALL :: List.tabulate(6)(_ => DOT) ::: List.tabulate(2)(_ => WALL) ::: List.tabulate(4)(_ => DOT) ::: WALL :: Nil,
    List.tabulate(6)(_ => WALL) ::: DOT :: List.tabulate(5)(_ => WALL) ::: EMPTY_SPACE :: WALL :: Nil,
    List.tabulate(6)(_ => WALL) ::: DOT :: List.tabulate(5)(_ => WALL) ::: EMPTY_SPACE :: WALL :: Nil,
    List.tabulate(6)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: List.tabulate(5)(_ => EMPTY_SPACE),
    List.tabulate(6)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: EMPTY_SPACE :: List.tabulate(3)(_ => WALL) ::: EMPTY_SPACE :: Nil,
    List.tabulate(6)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: EMPTY_SPACE :: WALL :: List.tabulate(3)(_ => EMPTY_SPACE),
    List.tabulate(6)(_ => EMPTY_SPACE) ::: DOT :: List.tabulate(3)(_ => EMPTY_SPACE) ::: WALL :: List.tabulate(3)(_ => EMPTY_SPACE),
    List.tabulate(6)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: EMPTY_SPACE :: WALL :: List.tabulate(3)(_ => EMPTY_SPACE),
    List.tabulate(6)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: EMPTY_SPACE :: List.tabulate(4)(_ => WALL),
    List.tabulate(6)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: List.tabulate(5)(_ => EMPTY_SPACE),
    List.tabulate(6)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: EMPTY_SPACE :: List.tabulate(4)(_ => WALL),
    List.tabulate(6)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: EMPTY_SPACE :: List.tabulate(4)(_ => WALL),
    WALL :: List.tabulate(12)(_ => DOT) ::: WALL :: Nil,
    WALL :: DOT :: List.tabulate(4)(_ => WALL) ::: DOT :: List.tabulate(5)(_ => WALL) ::: DOT :: WALL :: Nil,
    WALL :: DOT :: List.tabulate(4)(_ => WALL) ::: DOT :: List.tabulate(5)(_ => WALL) ::: DOT :: WALL :: Nil,
    WALL :: ENERGIZER_DOT :: List.tabulate(2)(_ => DOT) ::: List.tabulate(2)(_ => WALL) ::: List.tabulate(7)(_ => DOT) ::: EMPTY_SPACE :: Nil,
    List.tabulate(3)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: DOT :: List.tabulate(4)(_ => WALL),
    List.tabulate(3)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: DOT :: List.tabulate(2)(_ => WALL) ::: DOT :: List.tabulate(4)(_ => WALL),
    WALL :: List.tabulate(6)(_ => DOT) ::: List.tabulate(2)(_ => WALL) ::: List.tabulate(4)(_ => DOT) ::: WALL :: Nil,
    WALL :: DOT :: List.tabulate(10)(_ => WALL) ::: DOT :: WALL :: Nil,
    WALL :: DOT :: List.tabulate(10)(_ => WALL) ::: DOT :: WALL :: Nil,
    WALL :: List.tabulate(13)(_ => DOT),
    List.tabulate(14)(_ => WALL),
  ).map(Utility.mirrorList)

  "PacmanMap" should {
    "convert a Map object in a PacmanMap object" in {
      PacmanMap.toPacmanMap(Map.create(MapType.CLASSIC)) shouldEqual mapClassicBuilt
      PacmanMap.toPacmanMap(Map(tiles = (Track(Some(Fruit.CHERRIES)) :: TrackSafe(Some(Fruit.GALAXIAN)) :: Nil) :: Nil)) shouldEqual
        ((CHERRIES_CODE, None) :: (GALAXIAN_CODE, None) :: Nil) :: Nil
    }

    "create a PacmanMap object from a Map object with the info of the characters" in {
      val map: Map = Map(List(
        List.tabulate(3)(Map.smallDot) ::: Map.energizerDot() :: Map.emptyTrack() :: Nil,
        List.tabulate(5)(Map.emptyTrack)
      ))

      val expectedMap: PacmanMap = List(
        List.tabulate(3)(_ => DOT) ::: ENERGIZER_DOT :: EMPTY_SPACE :: Nil,
        (ElementsCode.PACMAN_UP_CODE, Some(PACMAN_STYLE)) :: (ElementsCode.GHOST_CODE_BLINKY + ElementsCode.ARROW_UP_CODE, Some(BLINKY_STYLE)) ::
          (ElementsCode.GHOST_CODE_PINKY + ElementsCode.ARROW_UP_CODE, Some(PINKY_STYLE)) :: (ElementsCode.GHOST_CODE_INKY + ElementsCode.ARROW_UP_CODE, Some(INKY_STYLE)) ::
          (ElementsCode.GHOST_CODE_CLYDE + ElementsCode.ARROW_UP_CODE, Some(CLYDE_STYLE)) :: Nil,
      )

      val gameEntities: Set[GameEntityDTO] = Set(
        GameEntityDTO(GameCharacterHolder(GameCharacter.PACMAN), Point2D(0, 9), 1, isDead = false, DirectionHolder(Direction.NORTH)),
        GameEntityDTO(GameCharacterHolder(GameCharacter.BLINKY), Point2D(9, 9), 1, isDead = false, DirectionHolder(Direction.NORTH)),
        GameEntityDTO(GameCharacterHolder(GameCharacter.PINKY), Point2D(18, 9), 1, isDead = false, DirectionHolder(Direction.NORTH)),
        GameEntityDTO(GameCharacterHolder(GameCharacter.INKY), Point2D(27, 9), 1, isDead = false, DirectionHolder(Direction.NORTH)),
        GameEntityDTO(GameCharacterHolder(GameCharacter.CLYDE), Point2D(36, 9), 1, isDead = false, DirectionHolder(Direction.NORTH))
      )

      PacmanMap.createWithCharacters(map, gameEntities) shouldEqual expectedMap
    }
  }
  // scalastyle:on
}
