package it.unibo.scalapacman.client.input

import java.awt.event.ActionEvent

import it.unibo.scalapacman.client.controller.Action.MOVEMENT
import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.common.MoveCommandType.MoveCommandType
import javax.swing.AbstractAction

case class GameAction(moveCommand: MoveCommandType)(implicit controller: Controller) extends AbstractAction {
  override def actionPerformed(actionEvent: ActionEvent): Unit = controller.handleAction(MOVEMENT, Some(moveCommand))
}
