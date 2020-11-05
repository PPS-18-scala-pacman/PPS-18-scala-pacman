package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.math.Point2D
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.lib.model.PlayerType.PlayerType

trait Character extends GameObject {
  val position: Point2D
  val speed: Double
  val direction: Direction
  val isDead: Boolean
}

object Character {

  case class Pacman(
                     override val position: Point2D,
                     override val speed: Double,
                     override val direction: Direction,
                     override val isDead: Boolean = false,
                     playerType: PlayerType = PlayerType.PLAYER_ONE
                   ) extends Character

  case class Ghost(
                    ghostType: GhostType,
                    override val position: Point2D,
                    override val speed: Double,
                    override val direction: Direction,
                    override val isDead: Boolean = false
                  ) extends Character

  object Ghost {
    val POINTS = 200
  }

  /**
   * Applica la giusta funzione di copy in base all'istanza dell'oggetto character passato in input.
   * @param character Oggetto da copiare
   * @param position Nuova posizione. Opzionale.
   * @param speed Nuova velocità. Opzionale.
   * @param direction Nuova direzione. Opzionale.
   * @param isDead Nuovo stato isDead. Opzionale.
   * @return Una copia aggiornata dell'oggetto character in input
   */
  def copy(character: Character)(
    position: Point2D = character.position,
    speed: Double = character.speed,
    direction: Direction = character.direction,
    isDead: Boolean = character.isDead
  ): Character = character match {
    case pacman: Pacman => pacman.copy(position = position, speed = speed, direction = direction, isDead = isDead)
    case ghost: Ghost => ghost.copy(position = position, speed = speed, direction = direction, isDead = isDead)
    case _ => throw new IllegalArgumentException("Unknown character type")
  }
}
