package it.unibo.scalapacman.client.input

import it.unibo.scalapacman.client.controller.Controller
import it.unibo.scalapacman.client.input.KeyStrokeIdentifier.{DOWN_PRESSED, DOWN_RELEASED, LEFT_PRESSED, LEFT_RELEASED,
  RIGHT_PRESSED, RIGHT_RELEASED, UP_PRESSED, UP_RELEASED, PAUSE_RESUME}
import it.unibo.scalapacman.common.MoveCommandType
import javax.swing.{ActionMap, InputMap, KeyStroke}

object UserInput {
  /**
   * Associa ad ogni tasto della configurazione attuale il relativo comportamento, eseguendo
   * l'accoppiamento sulla InputMap e l'ActionMap del componente da configurare
   *
   * @param im input map del componente da configurare
   * @param am action map del componente da configurare
   * @param keyMap configurazione tasti da utilizzare per configurare la input map e la action map
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
      (KeyStroke.getKeyStroke(keyMap.pause, 0, true), PAUSE_RESUME) ::
      Nil

    inputMapElementsList foreach { imel => addToInputMap(im, imel) }

    val actionMapElementsList: List[(KeyStrokeIdentifier, GameAction)] =
      (UP_PRESSED, GameMovement(MoveCommandType.UP)) ::
      (UP_RELEASED, GameMovement(MoveCommandType.NONE)) ::
      (DOWN_PRESSED, GameMovement(MoveCommandType.DOWN)) ::
      (DOWN_RELEASED, GameMovement(MoveCommandType.NONE)) ::
      (RIGHT_PRESSED, GameMovement(MoveCommandType.RIGHT)) ::
      (RIGHT_RELEASED, GameMovement(MoveCommandType.NONE)) ::
      (LEFT_PRESSED, GameMovement(MoveCommandType.LEFT)) ::
      (LEFT_RELEASED, GameMovement(MoveCommandType.NONE)) ::
      (PAUSE_RESUME, GamePause()) ::
      Nil

    actionMapElementsList foreach { amel => addToActionMap(am, amel) }
  }

  private def addToInputMap(im: InputMap, imel: (KeyStroke, KeyStrokeIdentifier)): Unit = im put (imel._1, imel._2)

  private def addToActionMap(am: ActionMap, amel: (KeyStrokeIdentifier, GameAction)): Unit = am put (amel._1, amel._2)
}
