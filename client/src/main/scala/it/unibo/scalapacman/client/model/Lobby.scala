package it.unibo.scalapacman.client.model

case class Lobby(id: String, description: String, size: Int, attendees: List[String]) {
  override def toString: String = s"$description                 (${attendees.size}/$size)"
}
