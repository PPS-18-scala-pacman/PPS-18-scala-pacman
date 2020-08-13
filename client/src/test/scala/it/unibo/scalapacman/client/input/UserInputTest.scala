package it.unibo.scalapacman.client.input

import java.awt.event.KeyEvent

import it.unibo.scalapacman.client.controller.{Action, Controller}
import it.unibo.scalapacman.client.model.GameModel
import it.unibo.scalapacman.common.MoveCommandType
import it.unibo.scalapacman.common.MoveCommandType.MoveCommandType
import it.unibo.scalapacman.lib.model.Map
import javax.swing.{JComponent, JTextField}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UserInputTest extends AnyWordSpec with Matchers {

  val _keyMap: KeyMap = KeyMap(KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT)
  var _component: JComponent = _
  implicit var _controller: Controller = _
  var _keyStrokeIdentifiers: List[KeyStrokeIdentifier] = _
  var _gameActions: List[GameAction] = _

  private case class ControllerMock() extends Controller {
    override def handleAction(action: Action, param: Option[Any]): Unit = Unit
    override def userAction: Option[MoveCommandType] = None
    override var model: GameModel = GameModel(None, _keyMap, Map.classic)
  }

  "UserInput" should {
    "apply key binding" in {
      _controller = ControllerMock()
      _component = new JTextField()

      val inputMap = _component.getInputMap
      val actionMap = _component.getActionMap

      UserInput.setupUserInput(inputMap, actionMap, _keyMap)

      _keyStrokeIdentifiers = KeyStrokeIdentifier.UP_PRESSED :: KeyStrokeIdentifier.UP_RELEASED ::
        KeyStrokeIdentifier.DOWN_PRESSED :: KeyStrokeIdentifier.DOWN_RELEASED ::
        KeyStrokeIdentifier.RIGHT_PRESSED :: KeyStrokeIdentifier.RIGHT_RELEASED ::
        KeyStrokeIdentifier.LEFT_PRESSED :: KeyStrokeIdentifier.LEFT_RELEASED :: Nil

      for (i <- inputMap.keys().indices) {
        inputMap.get(inputMap.keys()(i)) shouldEqual _keyStrokeIdentifiers(i)
      }

      _gameActions = GameAction(MoveCommandType.UP) :: GameAction(MoveCommandType.NONE) ::
        GameAction(MoveCommandType.DOWN) :: GameAction(MoveCommandType.NONE) ::
        GameAction(MoveCommandType.RIGHT) :: GameAction(MoveCommandType.NONE) ::
        GameAction(MoveCommandType.LEFT) :: GameAction(MoveCommandType.NONE) :: Nil

      for (i <- actionMap.keys().indices) {
        actionMap.get(actionMap.keys()(i)) shouldEqual _gameActions(i)
      }
    }
  }
}
