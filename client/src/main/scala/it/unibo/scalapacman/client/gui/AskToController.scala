package it.unibo.scalapacman.client.gui

import it.unibo.scalapacman.client.controller.{Action, Controller}

trait AskToController {
  def askToController(action: Action, param: Option[Any])(implicit controller: Controller): Unit =
    controller.handleAction(action, param)
}
