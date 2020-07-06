package it.unibo.scalapacman.lib.math

/**
 * A Tile is composed by eight blocks x eight blocks.
 * It's center must be the center of the block at (3,4).
 */
object TileGeography {
  val SIZE: Double = 8 * BlockGeography.SIZE
  val LAST_BLOCK: Double = SIZE - BlockGeography.SIZE

  /**
   * The center block relative to a Tile
   *
   * @return The central block
   */
  private val middleVertex: Double = (LAST_BLOCK / 2).floor
  val centralBlock: Point2D = Point2D(middleVertex, LAST_BLOCK - middleVertex)
  val westBlock: Point2D = Point2D(0, centralBlock.y)
  val eastBlock: Point2D = Point2D(LAST_BLOCK, centralBlock.y)
  val northBlock: Point2D = Point2D(centralBlock.x, 0)
  val southBlock: Point2D = Point2D(centralBlock.x, LAST_BLOCK)

  val center: Point2D = centralBlock + BlockGeography.center
  val westBlockCenter: Point2D = westBlock + BlockGeography.center
  val eastBlockCenter: Point2D = eastBlock + BlockGeography.center
  val northBlockCenter: Point2D = northBlock + BlockGeography.center
  val southBlockCenter: Point2D = southBlock + BlockGeography.center

  val westGate: Point2D = westBlock + BlockGeography.westGate
  val eastGate: Point2D = eastBlock + BlockGeography.eastGate
  val northGate: Point2D = northBlock + BlockGeography.northGate
  val southGate: Point2D = southBlock + BlockGeography.southGate
}
