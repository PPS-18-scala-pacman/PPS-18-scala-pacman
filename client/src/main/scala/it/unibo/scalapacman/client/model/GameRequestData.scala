package it.unibo.scalapacman.client.model

case class CreateGameData(nickname: String, players: Int)
case class JoinGameData(nickname: String, lobbyId: Int)
