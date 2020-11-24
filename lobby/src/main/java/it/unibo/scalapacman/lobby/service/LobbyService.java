package it.unibo.scalapacman.lobby.service;

import it.unibo.scalapacman.lobby.model.Lobby;
import it.unibo.scalapacman.lobby.dao.Dao;
import rx.Single;

import java.util.*;

public class LobbyService {
  private final Dao<Lobby, Long> dao;
  private final LobbyStreamService streamService;

  public LobbyService(final Dao<Lobby, Long> dao, final LobbyStreamService streamService) {
    this.dao = dao;
    this.streamService = streamService;
  }

  public Single<List<Lobby>> getAll() {
    return this.dao.getAll();
  }

  public Single<Lobby> get(Long id) {
    return this.dao.get(id);
  }

  public Single<Lobby> create(Lobby lobby) {
    return this.dao.create(lobby).doOnSuccess(this.streamService::updateStreams);
  }

  public Single<Lobby> update(Long id, Lobby lobby) {
    return this.dao.update(id, lobby).doOnSuccess(this.streamService::updateStreams);
  }

  public Single<Lobby> delete(Long id) {
    return this.dao.delete(id).doOnSuccess(entity -> this.streamService.updateStreams(entity, true));
  }
}
