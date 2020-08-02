package it.unibo.scalapacman.common

import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import it.unibo.scalapacman.lib.model.Fruit.APPLE
import it.unibo.scalapacman.lib.model.{Dot, Map}
import it.unibo.scalapacman.lib.model.Tile.Track

// scalastyle:off magic.number
class MapUpdaterTest extends AnyWordSpec with BeforeAndAfterAll with Matchers {

  val MAP_SIZE: Int = 6

  var map: Map = _
  var expectedMap: Map = _
  var dots: Set[DotDTO] = _
  var fruit: Option[FruitDTO] = _

  override def beforeAll(): Unit = {
    map = Map(List(List.tabulate(MAP_SIZE - 1)(Map.smallDot) ::: Map.emptyTrack() :: Nil))
  }

  "MapUpdater" should {
    "update map" when {
      "missing dots" in {
        dots = Set(
          DotDTO(DotHolder(Dot.SMALL_DOT), (1, 0)),
          DotDTO(DotHolder(Dot.SMALL_DOT), (2, 0)),
          DotDTO(DotHolder(Dot.SMALL_DOT), (3, 0)),
          DotDTO(DotHolder(Dot.SMALL_DOT), (4, 0))
        )

        fruit = None

        expectedMap = Map(List(Map.emptyTrack() :: List.tabulate(MAP_SIZE - 2)(Map.smallDot) ::: Map.emptyTrack() :: Nil))

        MapUpdater.update(map, dots, fruit) shouldEqual expectedMap

        dots = Set(
          DotDTO(DotHolder(Dot.SMALL_DOT), (2, 0)),
          DotDTO(DotHolder(Dot.SMALL_DOT), (3, 0)),
          DotDTO(DotHolder(Dot.SMALL_DOT), (4, 0))
        )

        expectedMap = Map(List(List.tabulate(2)(Map.emptyTrack) ::: List.tabulate(MAP_SIZE - 3)(Map.smallDot) ::: Map.emptyTrack() :: Nil))

        MapUpdater.update(map, dots, fruit) shouldEqual expectedMap

        dots = Set.empty[DotDTO]

        expectedMap = Map(List(List.tabulate(MAP_SIZE)(Map.emptyTrack)))

        MapUpdater.update(map, dots, fruit) shouldEqual expectedMap
      }

      "adding a fruit" in {
        dots = Set(
          DotDTO(DotHolder(Dot.SMALL_DOT), (0, 0)),
          DotDTO(DotHolder(Dot.SMALL_DOT), (1, 0)),
          DotDTO(DotHolder(Dot.SMALL_DOT), (2, 0)),
          DotDTO(DotHolder(Dot.SMALL_DOT), (3, 0)),
          DotDTO(DotHolder(Dot.SMALL_DOT), (4, 0))
        )

        fruit = Some(FruitDTO(FruitHolder(APPLE), (MAP_SIZE - 1, 0)))

        expectedMap = Map(List(List.tabulate(MAP_SIZE - 1)(Map.smallDot) ::: Track(Some(APPLE)) :: Nil))

        MapUpdater.update(map, dots, fruit) shouldEqual expectedMap
      }

      "adding and then removing a fruit" in {
        dots = Set(
          DotDTO(DotHolder(Dot.SMALL_DOT), (0, 0)),
          DotDTO(DotHolder(Dot.SMALL_DOT), (1, 0)),
          DotDTO(DotHolder(Dot.SMALL_DOT), (2, 0)),
          DotDTO(DotHolder(Dot.SMALL_DOT), (3, 0)),
          DotDTO(DotHolder(Dot.SMALL_DOT), (4, 0))
        )

        fruit = Some(FruitDTO(FruitHolder(APPLE), (MAP_SIZE - 1, 0)))

        expectedMap = Map(List(List.tabulate(MAP_SIZE - 1)(Map.smallDot) ::: Track(Some(APPLE)) :: Nil))

        MapUpdater.update(map, dots, fruit) shouldEqual expectedMap

        fruit = None

        expectedMap = Map(List(List.tabulate(MAP_SIZE - 1)(Map.smallDot) ::: Map.emptyTrack() :: Nil))

        MapUpdater.update(map, dots, fruit) shouldEqual expectedMap
      }
    }
  }
}
// scalastyle:on magic.number
