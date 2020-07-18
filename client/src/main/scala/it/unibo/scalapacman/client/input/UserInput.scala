package it.unibo.scalapacman.client.input

import java.awt.event.ActionEvent

import it.unibo.scalapacman.client.controller.{Controller, UserAction}
import it.unibo.scalapacman.client.input.KeyStrokeIdentifier.{DOWN_PRESSED, DOWN_RELEASED, LEFT_PRESSED, LEFT_RELEASED,
  RIGHT_PRESSED, RIGHT_RELEASED, UP_PRESSED, UP_RELEASED}
import it.unibo.scalapacman.client.controller.Action.{MOVEMENT, MOVE_DEFAULT, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT, MOVE_UP}
import javax.swing.{AbstractAction, ActionMap, InputMap, KeyStroke}

object UserInput {
  /**
   * Imposta
   *
   * @param im  input map del componente da configurare
   * @param am  action map del componente da configurare
   * @param keyMap  binding map da utilizzare per configurare la input map e la action map
   */
  def setupUserInput(im: InputMap, am: ActionMap, keyMap: KeyMap)(implicit controller: Controller): Unit = {
    im.clear()
    am.clear()

    val inputMapElementsList: List[(KeyStroke, KeyStrokeIdentifier)] =
      (KeyStroke.getKeyStroke(keyMap.up, 0, false), UP_PRESSED) ::
      (KeyStroke.getKeyStroke(keyMap.up, 0, true), UP_RELEASED) ::
      (KeyStroke.getKeyStroke(keyMap.down, 0, false), DOWN_PRESSED) ::
      (KeyStroke.getKeyStroke(keyMap.down, 0, true), DOWN_RELEASED) ::
      (KeyStroke.getKeyStroke(keyMap.right, 0, false), RIGHT_PRESSED) ::
      (KeyStroke.getKeyStroke(keyMap.right, 0, true), RIGHT_RELEASED) ::
      (KeyStroke.getKeyStroke(keyMap.left, 0, false), LEFT_PRESSED) ::
      (KeyStroke.getKeyStroke(keyMap.left, 0, true), LEFT_RELEASED) ::
      Nil

    inputMapElementsList foreach { imel => addToInputMap(im, imel) }

    val actionMapElementsList: List[(KeyStrokeIdentifier, GameAction)] =
      (UP_PRESSED, GameAction(MOVE_UP)) ::
      (UP_RELEASED, GameAction(MOVE_DEFAULT)) ::
      (DOWN_PRESSED, GameAction(MOVE_DOWN)) ::
      (DOWN_RELEASED, GameAction(MOVE_DEFAULT)) ::
      (RIGHT_PRESSED, GameAction(MOVE_RIGHT)) ::
      (RIGHT_RELEASED, GameAction(MOVE_DEFAULT)) ::
      (LEFT_PRESSED, GameAction(MOVE_LEFT)) ::
      (LEFT_RELEASED, GameAction(MOVE_DEFAULT)) ::
      Nil

    actionMapElementsList foreach { amel => addToActionMap(am, amel) }
  }

  private def addToInputMap(im: InputMap, imel: (KeyStroke, KeyStrokeIdentifier)): Unit = im put (imel._1, imel._2)

  private def addToActionMap(am: ActionMap, amel: (KeyStrokeIdentifier, GameAction)): Unit = am put (amel._1, amel._2)

  private case class GameAction(userAction: UserAction)(implicit controller: Controller) extends AbstractAction {
    override def actionPerformed(actionEvent: ActionEvent): Unit = controller.handleAction(MOVEMENT, Some(userAction))
  }
}
