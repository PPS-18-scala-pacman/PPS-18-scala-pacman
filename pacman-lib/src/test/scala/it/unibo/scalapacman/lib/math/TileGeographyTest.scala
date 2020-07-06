package it.unibo.scalapacman.lib.math

import org.scalatest.wordspec.AnyWordSpec

class TileGeographyTest extends AnyWordSpec {
  val CENTER: Point2D = Point2D(3.5, 4.5)
  val WEST_GATE: Point2D = Point2D(0, CENTER.y)
  val EAST_GATE: Point2D = Point2D(TileGeography.SIZE, CENTER.y)
  val NORTH_GATE: Point2D = Point2D(CENTER.x, 0)
  val SOUTH_GATE: Point2D = Point2D(CENTER.x, TileGeography.SIZE)

  "A tile" must {
    "have a center" in {
      assertResult(CENTER.x)(TileGeography.center.x)
      assertResult(CENTER.y)(TileGeography.center.y)
    }
    "have an east gate" in {
      assertResult(EAST_GATE.x)(TileGeography.eastGate.x)
      assertResult(EAST_GATE.y)(TileGeography.eastGate.y)
    }
    "have a west gate" in {
      assertResult(WEST_GATE.x)(TileGeography.westGate.x)
      assertResult(WEST_GATE.y)(TileGeography.westGate.y)
    }
    "have a north gate" in {
      assertResult(NORTH_GATE.x)(TileGeography.northGate.x)
      assertResult(NORTH_GATE.y)(TileGeography.northGate.y)
    }
    "have a south gate" in  {
      assertResult(SOUTH_GATE.x)(TileGeography.southGate.x)
      assertResult(SOUTH_GATE.y)(TileGeography.southGate.y)
    }
  }
}
