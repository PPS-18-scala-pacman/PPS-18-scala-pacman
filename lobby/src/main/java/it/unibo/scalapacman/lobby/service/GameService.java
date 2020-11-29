package it.unibo.scalapacman.lobby.service;

import it.unibo.scalapacman.lobby.communication.GameActions;
import it.unibo.scalapacman.lobby.model.Game;
import it.unibo.scalapacman.lobby.model.Lobby;
import it.unibo.scalapacman.lobby.util.REST;
import it.unibo.scalapacman.lobby.util.SSE;

public class GameService {

  public GameService(final GameActions gameActions, final LobbyStreamService lobbyStreamService, final LobbyService lobbyService) {
    LobbyStreamEventType type = new LobbyStreamEventType(LobbyStreamObject.Lobby, REST.Update);

    lobbyStreamService.getStream()
      .map(SSE.Event::getData)
      .filter(lobby -> lobby != null && lobby.getParticipants() != null && lobby.getSize() == lobby.getParticipants().size())
      .flatMap(lobby ->
        gameActions.startGame(new Game(lobby))
          .map(game -> new Lobby(lobby.getId(), lobby.getDescription(), lobby.getSize(), lobby.getHostUsername(), lobby.getParticipants(), game.getId()))
          .toObservable()
      )
      .doOnNext(lobby -> lobbyStreamService.updateStreams(lobby.getId(), lobby, type))
      .flatMap(lobby -> lobbyService.delete(lobby.getId()).toObservable())
      .subscribe();
  }

}
