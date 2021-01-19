package it.unibo.scalapacman.common
import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import it.unibo.scalapacman.lib.model.{Character, Direction, Dot, Fruit, GhostType}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UpdateModelDTOTest extends AnyWordSpec with BeforeAndAfterAll with Matchers {

  var pacman: Character = _
  var blinky: Character = _
  var pinky: Character = _
  var inky: Character = _
  var clyde: Character = _
  var dot: DotDTO = _
  var fruit: FruitDTO = _

  // scalastyle:off magic.number
  override protected def beforeAll(): Unit = {
    pacman = Pacman(Point2D(0, 0), 1, Direction.EAST)
    blinky = Ghost(GhostType.BLINKY, Point2D(0, 0), 1, Direction.EAST)
    pinky  = Ghost(GhostType.PINKY, Point2D(0, 0), 1, Direction.EAST)
    inky   = Ghost(GhostType.INKY, Point2D(0, 0), 1, Direction.EAST)
    clyde  = Ghost(GhostType.CLYDE, Point2D(0, 0), 1, Direction.EAST)
    dot    = DotDTO(DotHolder(Dot.SMALL_DOT), (0, 0))
    fruit  = FruitDTO(FruitHolder(Fruit.APPLE), (0, 0))
  }

  "GameEntityDTO" should {
    "return correct character" in {
      val pacmanDTO = GameEntityDTO("1", GameCharacterHolder(GameCharacter.PACMAN), Point2D(0, 0), 1, isDead=false, DirectionHolder(Direction.EAST))
      pacmanDTO.toPacman shouldEqual Some(pacman)
      val blinkyDTO = GameEntityDTO("2", GameCharacterHolder(GameCharacter.BLINKY), Point2D(0, 0), 1, isDead=false, DirectionHolder(Direction.EAST))
      blinkyDTO.toGhost shouldEqual Some(blinky)
      val pinkyDTO = GameEntityDTO("3", GameCharacterHolder(GameCharacter.PINKY), Point2D(0, 0), 1, isDead=false, DirectionHolder(Direction.EAST))
      pinkyDTO.toGhost shouldEqual Some(pinky)
      val inkyDTO = GameEntityDTO("4", GameCharacterHolder(GameCharacter.INKY), Point2D(0, 0), 1, isDead=false, DirectionHolder(Direction.EAST))
      inkyDTO.toGhost shouldEqual Some(inky)
      val clydeDTO = GameEntityDTO("5", GameCharacterHolder(GameCharacter.CLYDE), Point2D(0, 0), 1, isDead=false, DirectionHolder(Direction.EAST))
      clydeDTO.toGhost shouldEqual Some(clyde)
    }
  }

  "DotDTO" should {
    "transform ((Int, Int), Dot.Value) in DotDTO" in {
      DotDTO.rawToDotDTO((0, 0), Dot.SMALL_DOT) shouldEqual dot
    }
  }

  "FruitDTO" should {
    "transform ((Int, Int), Fruit.Value) in FruitDTO" in {
      FruitDTO.rawToFruitDTO((0, 0), Fruit.APPLE) shouldEqual fruit
    }
  }
  // scalastyle:on magic.number
}
