package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import it.unibo.scalapacman.lib.engine.GameHelpers.MapHelper
import it.unibo.scalapacman.lib.model.PacmanType.{PACMAN, indexToPlayerTypeVal}
import it.unibo.scalapacman.lib.model.SpeedCondition.SpeedCondition
import it.unibo.scalapacman.lib.model.SpeedLevel.SpeedLevel

trait LevelGenerator {
  def map: Map

  def characters(numPlayers: Int): List[Character]

  def fruit: Fruit.Value

  def gameState: GameState
}

object Level {
  // scalastyle:off magic.number
  val BASE_SPEED = 0.07575757625

  case class Classic(level: Int) extends LevelGenerator {
    private val mapType = MapType.CLASSIC
    val map: Map = Map.create(mapType)

    def characters(numPlayers: Int): List[Character] =
      (0 until numPlayers).map(i => pacman(indexToPlayerTypeVal(i))).toList :::
      ghost(GhostType.BLINKY) :: ghost(GhostType.PINKY) ::
      ghost(GhostType.INKY) :: ghost(GhostType.CLYDE) ::
      Nil

    def pacman(pType: PacmanType.PacmanType = PACMAN): Pacman =
      Pacman(pType, Map.getStartPosition(mapType, Pacman, pType), pacmanSpeed(level), Direction.WEST)

    def ghost(gType: GhostType.GhostType): Ghost = gType match {
      case GhostType.BLINKY => Ghost(gType, Map.getStartPosition(mapType, Ghost, gType), ghostSpeed(level), Direction.WEST)
      case GhostType.PINKY  => Ghost(gType, Map.getStartPosition(mapType, Ghost, gType), ghostSpeed(level), Direction.NORTH)
      case GhostType.INKY   => Ghost(gType, Map.getStartPosition(mapType, Ghost, gType), ghostSpeed(level), Direction.EAST, isDead = true)
      case GhostType.CLYDE  => Ghost(gType, Map.getStartPosition(mapType, Ghost, gType), ghostSpeed(level), Direction.WEST, isDead = true)
    }

    def fruit: Fruit.Value = Level.fruit(level)

    def gameState: GameState = GameState(0)

    def gameEvents: List[GameTimedEvent[_]] =
      GameTimedEvent(GameTimedEventType.FRUIT_SPAWN, dots = Some(map.dots.size - 70), payload = Some(fruit)) ::
      GameTimedEvent(GameTimedEventType.FRUIT_SPAWN, dots = Some(map.dots.size - 170), payload = Some(fruit)) ::
      GameTimedEvent(GameTimedEventType.GHOST_RESTART, dots = Some(map.dots.size - 30), payload = Some(GhostType.INKY)) ::
      GameTimedEvent(GameTimedEventType.GHOST_RESTART, dots = Some(map.dots.size - 60), payload = Some(GhostType.CLYDE)) ::
      Nil
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

  def energizerDuration(level: Int): Int = (level match {
    case 1 => 6
    case 2 | 6 | 10 => 5
    case 3 => 4
    case 4 | 14 => 3
    case 5 | 7 | 8 | 11 => 2
    case 17 => 0
    case _ if level <= 18 => 1
    case _ => 0
  }) * 1000

  def ghostRespawnDotCounter(level: Int, ghostType: GhostType.GhostType): Int = ghostType match {
    case GhostType.INKY => 7
    case GhostType.CLYDE => 17
    case _ => 0
  }
}
