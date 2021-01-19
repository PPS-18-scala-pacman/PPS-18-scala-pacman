package it.unibo.scalapacman.client.model

case class CreateLobbyData(username: String, size: Int)
case class JoinLobbyData(username: String, lobby: Lobby)
