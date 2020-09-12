package it.unibo.scalapacman.client.input

import java.awt.event.ActionEvent

import it.unibo.scalapacman.client.controller.Action.{MOVEMENT, PAUSE_RESUME}
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.common.CommandType
import it.unibo.scalapacman.common.CommandType.CommandType
import it.unibo.scalapacman.common.MoveCommandType.MoveCommandType
import javax.swing.AbstractAction

/**
 * Rappresenta un'azione dell'utente quando preme uno dei comandi da tastiera
 */
trait GameAction extends AbstractAction

/**
 * Implementa l'azione da eseguire quando l'utente esegue un comando di movimento
 * @param moveCommand tipologia di movimento
 * @param controller istanza del controller a cui notificare l'azione
 */
case class GameMovement(moveCommand: MoveCommandType)(implicit controller: Controller) extends GameAction {
  override def actionPerformed(actionEvent: ActionEvent): Unit = controller.handleAction(MOVEMENT, Some(moveCommand))
}

/**
 * Implementa l'azione da eseguire quando l'utente esegue il comando di pausa / ripresa gioco
 * @param controller istanza del controller a cui notificare l'azione
 */
case class GamePause()(implicit controller: Controller) extends GameAction {
  override def actionPerformed(e: ActionEvent): Unit = controller.handleAction(PAUSE_RESUME, pauseOrResume())

  def pauseOrResume(): Option[CommandType] = if (controller.model.paused) Some(CommandType.RESUME) else Some(CommandType.PAUSE)
}
