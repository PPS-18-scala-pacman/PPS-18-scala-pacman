package it.unibo.scalapacman.client.map

import org.scalatest.wordspec.AnyWordSpecLike
import it.unibo.scalapacman.client.map.ElementsCode.{WALL_CODE, DOT_CODE, ENERGIZED_DOT_CODE, EMPTY_SPACE_CODE}
import it.unibo.scalapacman.client.map.PacmanMap.PacmanMap
import it.unibo.scalapacman.lib.Utility

class MapBuilderTest
  extends AnyWordSpecLike {

  // scalastyle:off
  val mapClassicBuilt: PacmanMap = List[List[String]](
    List.tabulate(14)(_ => WALL_CODE),
    WALL_CODE :: List.tabulate(12)(_ => DOT_CODE) ::: WALL_CODE :: Nil,
    WALL_CODE :: DOT_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: DOT_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: ENERGIZED_DOT_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: DOT_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: DOT_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: DOT_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: List.tabulate(13)(_ => DOT_CODE),
    WALL_CODE :: DOT_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(4)(_ => WALL_CODE),
    WALL_CODE :: DOT_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(4)(_ => WALL_CODE),
    WALL_CODE :: List.tabulate(6)(_ => DOT_CODE) ::: List.tabulate(2)(_ => WALL_CODE) ::: List.tabulate(4)(_ => DOT_CODE) ::: WALL_CODE :: Nil,
    List.tabulate(6)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: Nil,
    List.tabulate(6)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: Nil,
    List.tabulate(6)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: List.tabulate(5)(_ => EMPTY_SPACE_CODE),
    List.tabulate(6)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(3)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: Nil,
    List.tabulate(6)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: List.tabulate(3)(_ => EMPTY_SPACE_CODE),
    List.tabulate(6)(_ => EMPTY_SPACE_CODE) ::: DOT_CODE :: List.tabulate(3)(_ => EMPTY_SPACE_CODE) ::: WALL_CODE :: List.tabulate(3)(_ => EMPTY_SPACE_CODE),
    List.tabulate(6)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: List.tabulate(3)(_ => EMPTY_SPACE_CODE),
    List.tabulate(6)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE),
    List.tabulate(6)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: List.tabulate(5)(_ => EMPTY_SPACE_CODE),
    List.tabulate(6)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE),
    List.tabulate(6)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE),
    WALL_CODE :: List.tabulate(12)(_ => DOT_CODE) ::: WALL_CODE :: Nil,
    WALL_CODE :: DOT_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: DOT_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: DOT_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: DOT_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: ENERGIZED_DOT_CODE :: List.tabulate(2)(_ => DOT_CODE) ::: List.tabulate(2)(_ => WALL_CODE) ::: List.tabulate(7)(_ => DOT_CODE) ::: EMPTY_SPACE_CODE :: Nil,
    List.tabulate(3)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(4)(_ => WALL_CODE),
    List.tabulate(3)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: DOT_CODE :: List.tabulate(4)(_ => WALL_CODE),
    WALL_CODE :: List.tabulate(6)(_ => DOT_CODE) ::: List.tabulate(2)(_ => WALL_CODE) ::: List.tabulate(4)(_ => DOT_CODE) ::: WALL_CODE :: Nil,
    WALL_CODE :: DOT_CODE :: List.tabulate(10)(_ => WALL_CODE) ::: DOT_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: DOT_CODE :: List.tabulate(10)(_ => WALL_CODE) ::: DOT_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: List.tabulate(13)(_ => DOT_CODE),
    List.tabulate(14)(_ => WALL_CODE),
  ).map(Utility.mirrorList)
  // scalastyle:on

  "MapBuilder" should {
    "create classic map" in {
      assertResult(mapClassicBuilt)(MapBuilder.buildClassic())
    }
  }
}
