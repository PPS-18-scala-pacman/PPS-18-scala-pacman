package it.unibo.scalapacman.lobby.service;

import it.unibo.scalapacman.lobby.model.Lobby;
import it.unibo.scalapacman.lobby.dao.Dao;
import rx.Single;

import java.util.*;

public class LobbyService {
  private final Dao<Lobby, Long> repository;
  private final LobbyStreamService streamService;

  public LobbyService(final Dao<Lobby, Long> repository, final LobbyStreamService streamService) {
    this.repository = repository;
    this.streamService = streamService;
  }

  public Single<List<Lobby>> getAll() {
    return this.repository.getAll();
  }

  public Single<Lobby> get(Long id) {
    return this.repository.get(id);
  }

  public Single<Lobby> create(Lobby lobby) {
    return this.repository.create(lobby).doOnSuccess(this.streamService::updateStreams);
  }

  public Single<Lobby> update(Long id, Lobby lobby) {
    return this.repository.update(id, lobby).doOnSuccess(this.streamService::updateStreams);
  }

  public Single<Lobby> delete(Long id) {
    return this.repository.delete(id).doOnSuccess(entity -> this.streamService.updateStreams(entity, true));
  }
}
