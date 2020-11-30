package it.unibo.scalapacman.client.model

import it.unibo.scalapacman.lib.model.PacmanType
import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsString, JsValue, JsonFormat, RootJsonFormat, deserializationError}

case class Lobby(id: Int, description: String, size: Int, hostUsername: String, participants: List[Participant], gameId: Option[String]) {
  override def toString: String = s"$description - (${participants.size}/$size)"
}

case class Participant(username: String, pacmanType: PacmanType.PacmanType, lobbyId: Int) {
  override def toString: String = username
}

object LobbyJsonProtocol extends DefaultJsonProtocol {

  implicit object ParticipantJsonFormat extends RootJsonFormat[Participant] {
    def write(p: Participant): JsValue = JsObject(
      "lobbyId" -> JsNumber(p.lobbyId),
      "pacmanType" -> JsNumber(PacmanType.playerTypeValToIndex(p.pacmanType)),
      "username" -> JsString(p.username),
    )

    def read(value: JsValue): Participant = value.asJsObject.getFields("lobbyId", "pacmanType", "username") match {
      case Seq(JsNumber(lobbyId), JsNumber(pacmanType), JsString(username)) =>
        Participant(username, PacmanType.indexToPlayerTypeVal(pacmanType.toInt), lobbyId.toInt)
      case _ => deserializationError("Participant expected")
    }
  }

  implicit def lobbyFormat: JsonFormat[Lobby] = jsonFormat6(Lobby)
}

object LobbySSEEventType extends Enumeration {
  val LOBBY_CREATE: LobbySSEEventType.Value = Value("Lobby/Create")
  val LOBBY_DELETE: LobbySSEEventType.Value = Value("Lobby/Delete")
  val LOBBY_UPDATE: LobbySSEEventType.Value = Value("Lobby/Update")
  val PARTICIPANT_CREATE: LobbySSEEventType.Value = Value("Participant/Create")
  val PARTICIPANT_DELETE: LobbySSEEventType.Value = Value("Participant/Delete")
  val PARTICIPANT_UPDATE: LobbySSEEventType.Value = Value("Participant/Update")
}
