package it.unibo.scalapacman.client.input

import java.awt.event.KeyEvent

import it.unibo.scalapacman.client.controller.{Action, Controller}
import it.unibo.scalapacman.client.model.GameModel
import it.unibo.scalapacman.common.MoveCommandType
import it.unibo.scalapacman.common.MoveCommandType.MoveCommandType
import it.unibo.scalapacman.lib.model.{Map, MapType}
import javax.swing.{JComponent, JTextField}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UserInputTest extends AnyWordSpec with Matchers {

  val _keyMap: KeyMap = KeyMap(KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT, KeyEvent.VK_P)
  var _component: JComponent = _
  implicit var _controller: Controller = _
  var _keyStrokeIdentifiers: List[KeyStrokeIdentifier] = _
  var _gameActions: List[GameAction] = _

  private case class ControllerMock() extends Controller {
    override def handleAction(action: Action, param: Option[Any]): Unit = Unit
    override def userAction: Option[MoveCommandType] = None
    override var model: GameModel = GameModel(keyMap = _keyMap, map = Map.create(MapType.CLASSIC))
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
        KeyStrokeIdentifier.LEFT_PRESSED :: KeyStrokeIdentifier.LEFT_RELEASED :: KeyStrokeIdentifier.PAUSE_RESUME :: Nil

      for (i <- inputMap.keys().indices) {
        assert(_keyStrokeIdentifiers contains inputMap.get(inputMap.keys()(i)))
      }

      _gameActions = GameMovement(MoveCommandType.UP) :: GameMovement(MoveCommandType.NONE) ::
        GameMovement(MoveCommandType.DOWN) :: GameMovement(MoveCommandType.NONE) ::
        GameMovement(MoveCommandType.RIGHT) :: GameMovement(MoveCommandType.NONE) ::
        GameMovement(MoveCommandType.LEFT) :: GameMovement(MoveCommandType.NONE):: GamePause() :: Nil

      for (i <- actionMap.keys().indices) {
        assert(_gameActions contains actionMap.get(actionMap.keys()(i)))
      }
    }
  }
}
