package it.unibo.scalapacman.lobby.service;

import it.unibo.scalapacman.lobby.communication.GameActions;
import it.unibo.scalapacman.lobby.model.Game;

public class GameService {

  public GameService(final GameActions gameActions, final LobbyStreamService lobbyStreamService) {
    lobbyStreamService.getStream()
      .filter(event -> event.getData() != null && event.getData().getParticipants() != null && event.getData().getSize() == event.getData().getParticipants().size())
      .subscribe(event -> gameActions.startGame(new Game(event.getData())));
  }

}
