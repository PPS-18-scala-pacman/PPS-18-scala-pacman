package it.unibo.scalapacman.lib.model

trait Tile {
  val eatable: Option[Eatable]

  def walkable(character: Character): Boolean

  def speedModifier(character: Character, speed: Double): Double
}

object Tile {

  abstract class TileAbstract extends Tile {
    val eatable: Option[Eatable] = None

    def walkable(character: Character): Boolean = true

    def speedModifier(character: Character, speed: Double): Double = speed
  }

  case class Track(override val eatable: Option[Eatable]) extends TileAbstract()

  case class TrackSafe() extends TileAbstract() {
    override def walkable(character: Character): Boolean = character match {
      case Pacman(_, _, _, _) => true
      case _ => false
    }
  }

  case class TrackTunnel() extends TileAbstract() {
    override def speedModifier(character: Character, speed: Double): Double = character match {
      case Ghost(_, _, _, _, _) => speed / 2
      case _ => super.speedModifier(character, speed)
    }
  }

  case class GhostSpawn() extends TileAbstract() {
    override def walkable(character: Character): Boolean = character match {
      case Ghost(_, _, _, _, _) => true
      case _ => false
    }
  }

  case class Wall() extends TileAbstract() {
    override def walkable(character: Character): Boolean = false
  }

}
