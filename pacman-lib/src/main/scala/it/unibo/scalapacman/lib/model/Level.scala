package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.math.{Point2D, TileGeography}
import it.unibo.scalapacman.lib.engine.GameHelpers.MapHelper
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.lib.model.Map.MapIndexes
import it.unibo.scalapacman.lib.model.SpeedCondition.SpeedCondition
import it.unibo.scalapacman.lib.model.SpeedLevel.SpeedLevel

trait LevelGenerator {
  def map: Map

  def characters: List[Character]

  def fruit: (MapIndexes, Fruit.Value)

  def gameState: GameState
}

object Level {
  // scalastyle:off magic.number
  val BASE_SPEED = 0.07575757625

  case class Classic(level: Int) extends LevelGenerator {
    val map: Map = Map.classic

    val PACMAN_START_POSITION: Point2D = map.tileOrigin((14, 23)) + TileGeography.westBlockCenter
    val BLINKY_START_POSITION: Point2D = map.tileOrigin((13, 11)) + TileGeography.eastBlockCenter
    val PINKY_START_POSITION: Point2D = map.tileOrigin((11, 14)) + TileGeography.eastBlockCenter
    val INKY_START_POSITION: Point2D = map.tileOrigin((13, 15)) + TileGeography.eastBlockCenter
    val CLYDE_START_POSITION: Point2D = map.tileOrigin((15, 14)) + TileGeography.eastBlockCenter
    val FRUIT_INDEXES: MapIndexes = (14, 17)

    def characters: List[Character] = pacman :: ghost(GhostType.BLINKY) :: ghost(GhostType.PINKY) ::
      ghost(GhostType.INKY) :: ghost(GhostType.CLYDE) ::  Nil

    def pacman: Pacman = Pacman(PACMAN_START_POSITION, pacmanSpeed(level), Direction.WEST)

    def ghost(gType: GhostType): Ghost = gType match {
      case GhostType.BLINKY => Ghost(gType, BLINKY_START_POSITION, ghostSpeed(level), Direction.WEST)
      case GhostType.PINKY  => Ghost(gType, PINKY_START_POSITION,  ghostSpeed(level), Direction.EAST)
      case GhostType.INKY   => Ghost(gType, INKY_START_POSITION,   ghostSpeed(level), Direction.NORTH)
      case GhostType.CLYDE  => Ghost(gType, CLYDE_START_POSITION,  ghostSpeed(level), Direction.WEST)
    }

    def fruit: (MapIndexes, Fruit.Value) = (FRUIT_INDEXES, Level.fruit(level))

    def gameState: GameState = GameState(0)
  }

  def pacmanSpeed(level: Int, condition: SpeedCondition = SpeedCondition.NORM): Double = BASE_SPEED * ((speedLevel(level), condition) match {
    case (SpeedLevel.BEGINNER, SpeedCondition.FRIGHT) => 0.9
    case (SpeedLevel.BEGINNER, _) => 0.8
    case (SpeedLevel.EASY, SpeedCondition.FRIGHT) => 0.95
    case (SpeedLevel.NORMAL, _) => 1
    case _ => 0.9
  })

  // scalastyle:off cyclomatic.complexity
  def ghostSpeed(level: Int, condition: SpeedCondition = SpeedCondition.NORM): Double = BASE_SPEED * ((speedLevel(level), condition) match {
    case (SpeedLevel.BEGINNER, SpeedCondition.NORM) => 0.75
    case (SpeedLevel.BEGINNER, SpeedCondition.FRIGHT) => 0.50
    case (SpeedLevel.BEGINNER, SpeedCondition.TUNNEL) => 0.40
    case (SpeedLevel.EASY, SpeedCondition.NORM) => 0.85
    case (SpeedLevel.EASY, SpeedCondition.FRIGHT) => 0.55
    case (SpeedLevel.EASY, SpeedCondition.TUNNEL) => 0.45
    case (SpeedLevel.NORMAL, SpeedCondition.FRIGHT) => 0.60
    case (_, SpeedCondition.TUNNEL) => 0.5
    case _ => 0.95
  })
  // scalastyle:on cyclomatic.complexity

  private def speedLevel(level: Int): SpeedLevel = level match {
    case 1 => SpeedLevel.BEGINNER
    case 2 | 3 | 4 => SpeedLevel.EASY
    case it: Int if 5 to 20 contains it => SpeedLevel.NORMAL
    case _ => SpeedLevel.HARD
  }

  def fruit(level: Int): Fruit.Value = level match {
    case 1 => Fruit.CHERRIES
    case 2 => Fruit.STRAWBERRY
    case 3 | 4 => Fruit.PEACH
    case 5 | 6 => Fruit.APPLE
    case 7 | 8 => Fruit.GRAPES
    case 9 | 10 => Fruit.GALAXIAN
    case 11 | 12 => Fruit.BELL
    case _ => Fruit.KEY
  }
}
