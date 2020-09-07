package it.unibo.scalapacman.lib.math

/**
 * A Tile is composed by eight blocks x eight blocks.
 */
object TileGeography {
  val SIZE: Double = 8 * BlockGeography.SIZE
  val LAST_BLOCK: Double = SIZE - BlockGeography.SIZE

  private val middleVertex: Double = (LAST_BLOCK / 2).floor

  /**
   * The origin of the central block relative to a Tile
   *
   * @return The central block
   */
  val centralBlock: Point2D = Point2D(middleVertex, LAST_BLOCK - middleVertex)
  val westBlock: Point2D = Point2D(0, centralBlock.y)
  val eastBlock: Point2D = Point2D(LAST_BLOCK, centralBlock.y)
  val northBlock: Point2D = Point2D(centralBlock.x, 0)
  val southBlock: Point2D = Point2D(centralBlock.x, LAST_BLOCK)

  /**
   * Return center of the tile, which is the center of the block at (3,4).
   */
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
