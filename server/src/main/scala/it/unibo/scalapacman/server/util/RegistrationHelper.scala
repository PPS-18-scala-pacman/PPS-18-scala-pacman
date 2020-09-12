package it.unibo.scalapacman.server.util

import akka.actor.typed.ActorRef
import it.unibo.scalapacman.common.GameCharacter.{BLINKY, CLYDE, GameCharacter, INKY, PACMAN, PINKY}
import it.unibo.scalapacman.server.core.Engine.UpdateCommand
import it.unibo.scalapacman.server.model.{RegisteredParticipant, RegistrationModel}

/**
 * Contiene funzioni di utility a supporto della fase di iniziale di regitrazione degli attori alla partita
 */
object RegistrationHelper {

  /**
   * Restituisce un modello aggiornato a seguito della richiesta di registrazione di un nuovo patecipante
   *
   * @param model     modello di registrazione originale
   * @param charType  nuovo personaggio registrato
   * @param actor     riferimento al nuovo attore registrato
   * @return          modello di registrazione aggiornato
   */
  def registerPartecipant(model: RegistrationModel,
                          charType: GameCharacter,
                          actor: ActorRef[UpdateCommand]): RegistrationModel = updatePartecipant(model, charType, Some(actor))

  /**
   * Restituisce un modello aggiornato a seguito della richiesta di deregistrazione di un patecipante
   *
   * @param model     modello di registrazione originale
   * @param charType  personaggio da rimuovere
   * @return          modello di registrazione aggiornato
   */
  def unRegisterPartecipant(model: RegistrationModel,
                          charType: GameCharacter): RegistrationModel = updatePartecipant(model, charType, None)

  private def updatePartecipant(model: RegistrationModel,
                                charType: GameCharacter,
                                actor: Option[ActorRef[UpdateCommand]]): RegistrationModel =
    charType match {
      case PACMAN => model.copy(pacman = actor.map(RegisteredParticipant))
      case BLINKY => model.copy(blinky = actor.map(RegisteredParticipant))
      case INKY   => model.copy(inky   = actor.map(RegisteredParticipant))
      case PINKY  => model.copy(pinky  = actor.map(RegisteredParticipant))
      case CLYDE  => model.copy(clyde  = actor.map(RegisteredParticipant))
    }
}
