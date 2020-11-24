package it.unibo.scalapacman.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Lobby(id: Int, description: String, size: Int, participants: List[Participant]) {
  override def toString: String = s"$description - (${participants.size}/$size)"
}

case class Participant(username: String, host: Boolean, pacmanType: Int, lobbyId: Int)

object LobbyJsonProtocol extends DefaultJsonProtocol {

  implicit def participantFormat: JsonFormat[Participant] = jsonFormat4(Participant)

  implicit def lobbyFormat: JsonFormat[Lobby] = jsonFormat4(Lobby)
}
