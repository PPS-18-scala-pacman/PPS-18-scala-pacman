package it.unibo.scalapacman.lobby.service;

import it.unibo.scalapacman.lobby.communication.GameActions;
import it.unibo.scalapacman.lobby.model.Game;
import it.unibo.scalapacman.lobby.model.Lobby;
import it.unibo.scalapacman.lobby.util.REST;
import it.unibo.scalapacman.lobby.util.exception.UnauthorizedException;
import rx.Single;

public class GameService {

  private final GameActions gameActions;
  private final LobbyStreamService lobbyStreamService;
  private final LobbyService lobbyService;

  public GameService(final GameActions gameActions, final LobbyStreamService lobbyStreamService, final LobbyService lobbyService) {
    this.gameActions = gameActions;
    this.lobbyStreamService = lobbyStreamService;
    this.lobbyService = lobbyService;
  }

  public Single<Lobby> startGame(long lobbyId, String hostUsername) {
    return this.lobbyService.get(lobbyId)
      .doOnSuccess(lobby -> {
        if (!lobby.getHostUsername().equals(hostUsername)) {
          throw new UnauthorizedException(hostUsername + " is not the host. Only the host can start the game.");
        }
      })
      .flatMap(this::startGame);
  }

  private Single<Lobby> startGame(Lobby lobby) {
    LobbyStreamEventType type = new LobbyStreamEventType(LobbyStreamObject.Lobby, REST.Update);
    return gameActions.startGame(new Game(lobby))
      .map(game -> new Lobby(lobby.getId(), lobby.getDescription(), lobby.getSize(), lobby.getHostUsername(), lobby.getParticipants(), game.getId(), game.getHostId()))
      .doOnSuccess(lobbyResult -> lobbyStreamService.updateStreams(lobbyResult.getId(), lobbyResult, type))
      .flatMap(lobbyResult -> lobbyService.delete(lobbyResult.getId()));
  }
}
