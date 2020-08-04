package it.unibo.scalapacman.client.map

import org.scalatest.wordspec.AnyWordSpecLike
import it.unibo.scalapacman.client.map.ElementsCode.{EMPTY_SPACE_CODE, WALL_CODE}
import it.unibo.scalapacman.client.map.PacmanMap.PacmanMap
import it.unibo.scalapacman.common.{DirectionHolder, DotDTO, DotHolder, FruitDTO, FruitHolder, GameCharacter,
  GameCharacterHolder, GameEntityDTO, UpdateModelDTO}
import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit, GameState, Map}
import it.unibo.scalapacman.lib.Utility
import it.unibo.scalapacman.lib.math.Point2D
import org.scalatest.matchers.should.Matchers

class PacmanMapTest
  extends AnyWordSpecLike
  with Matchers {

  // scalastyle:off
  val mapClassicBuilt: PacmanMap = List(
    List.tabulate(14)(_ => WALL_CODE),
    WALL_CODE :: List.tabulate(12)(_ => EMPTY_SPACE_CODE) ::: WALL_CODE :: Nil,
    WALL_CODE :: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: List.tabulate(13)(_ => EMPTY_SPACE_CODE),
    WALL_CODE :: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE),
    WALL_CODE :: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE),
    WALL_CODE :: List.tabulate(6)(_ => EMPTY_SPACE_CODE) ::: List.tabulate(2)(_ => WALL_CODE) ::: List.tabulate(4)(_ => EMPTY_SPACE_CODE) ::: WALL_CODE :: Nil,
    List.tabulate(6)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: Nil,
    List.tabulate(6)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: Nil,
    List.tabulate(6)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: List.tabulate(5)(_ => EMPTY_SPACE_CODE),
    List.tabulate(6)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(3)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: Nil,
    List.tabulate(6)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: List.tabulate(3)(_ => EMPTY_SPACE_CODE),
    List.tabulate(7)(_ => EMPTY_SPACE_CODE) ::: List.tabulate(3)(_ => EMPTY_SPACE_CODE) ::: WALL_CODE :: List.tabulate(3)(_ => EMPTY_SPACE_CODE),
    List.tabulate(6)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: List.tabulate(3)(_ => EMPTY_SPACE_CODE),
    List.tabulate(6)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE),
    List.tabulate(6)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: List.tabulate(5)(_ => EMPTY_SPACE_CODE),
    List.tabulate(6)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE),
    List.tabulate(6)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE),
    WALL_CODE :: List.tabulate(12)(_ => EMPTY_SPACE_CODE) ::: WALL_CODE :: Nil,
    WALL_CODE :: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(5)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: List.tabulate(3)(_ => EMPTY_SPACE_CODE) ::: List.tabulate(2)(_ => WALL_CODE) ::: List.tabulate(8)(_ => EMPTY_SPACE_CODE) ::: Nil,
    List.tabulate(3)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE),
    List.tabulate(3)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(2)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: List.tabulate(4)(_ => WALL_CODE),
    WALL_CODE :: List.tabulate(6)(_ => EMPTY_SPACE_CODE) ::: List.tabulate(2)(_ => WALL_CODE) ::: List.tabulate(4)(_ => EMPTY_SPACE_CODE) ::: WALL_CODE :: Nil,
    WALL_CODE :: EMPTY_SPACE_CODE :: List.tabulate(10)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: EMPTY_SPACE_CODE :: List.tabulate(10)(_ => WALL_CODE) ::: EMPTY_SPACE_CODE :: WALL_CODE :: Nil,
    WALL_CODE :: List.tabulate(13)(_ => EMPTY_SPACE_CODE),
    List.tabulate(14)(_ => WALL_CODE),
  ).map(Utility.mirrorList)

  "PacmanMap" should {
    "convert a Map object in a PacmanMap object" in {
      PacmanMap.toPacmanMap(Map.classic) shouldEqual mapClassicBuilt
    }

    "create a PacmanMap object from a Map object and the info of the characters" in {
      val map: Map = Map(List(
        List.tabulate(3)(Map.smallDot) ::: Map.energizerDot() :: Map.emptyTrack() :: Nil,
        List.tabulate(5)(Map.emptyTrack)
      ))

      val expectedMap: PacmanMap = List(
        ElementsCode.EMPTY_SPACE_CODE :: List.tabulate(2)(_ => ElementsCode.DOT_CODE) ::: ElementsCode.ENERGIZER_DOT_CODE :: ElementsCode.APPLE_CODE :: Nil,
        ElementsCode.PACMAN_UP_CODE :: ElementsCode.GHOST_CODE_BLINKY + ElementsCode.ARROW_UP_CODE ::
          ElementsCode.GHOST_CODE_PINKY + ElementsCode.ARROW_UP_CODE :: ElementsCode.GHOST_CODE_INKY + ElementsCode.ARROW_UP_CODE ::
          ElementsCode.GHOST_CODE_CLYDE + ElementsCode.ARROW_UP_CODE :: Nil,
      )

      val gameEntities :Set[GameEntityDTO] = Set(
        GameEntityDTO(GameCharacterHolder(GameCharacter.PACMAN), Point2D(0, 9), 1, isDead=false, DirectionHolder(Direction.NORTH)),
        GameEntityDTO(GameCharacterHolder(GameCharacter.BLINKY), Point2D(9, 9), 1, isDead=false, DirectionHolder(Direction.NORTH)),
        GameEntityDTO(GameCharacterHolder(GameCharacter.PINKY), Point2D(18, 9), 1, isDead=false, DirectionHolder(Direction.NORTH)),
        GameEntityDTO(GameCharacterHolder(GameCharacter.INKY), Point2D(27, 9), 1, isDead=false, DirectionHolder(Direction.NORTH)),
        GameEntityDTO(GameCharacterHolder(GameCharacter.CLYDE), Point2D(36, 9), 1, isDead=false, DirectionHolder(Direction.NORTH))
      )

      val dots: Set[DotDTO] = Set(
        DotDTO(DotHolder(Dot.SMALL_DOT), (1, 0)),
        DotDTO(DotHolder(Dot.SMALL_DOT), (2, 0)),
        DotDTO(DotHolder(Dot.ENERGIZER_DOT), (3, 0))
      )

      val fruit = Some(FruitDTO(FruitHolder(Fruit.APPLE), (4, 0)))

      val model = UpdateModelDTO(gameEntities, GameState(0), dots, fruit)

      PacmanMap.createMap(map, model) shouldEqual expectedMap
    }
  }
  // scalastyle:on
}
