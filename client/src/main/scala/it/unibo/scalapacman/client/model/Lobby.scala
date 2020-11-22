package it.unibo.scalapacman.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class Lobby(id: Integer, description: String, size: Int, attendees: List[String]) {
  override def toString: String = s"$description - (${attendees.size}/$size)"
}

case class LobbyTemp(id: Int, description: String) {
  override def toString: String = s"$description"
}

object LobbyJsonProtocol extends DefaultJsonProtocol {
  implicit def lobbyFormat: JsonFormat[LobbyTemp] = jsonFormat2(LobbyTemp)
}
