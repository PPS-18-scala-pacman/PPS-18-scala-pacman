package it.unibo.scalapacman.lobby;

import rx.Single;

import java.util.List;

public class LobbyService {
  private final LobbyRepository repository;

  LobbyService(LobbyRepository repository) {
    this.repository = repository;
  }

  Single<List<Lobby>> getAll() {
    return this.repository.getAll();
  }

  Single<Lobby> get(Integer id) {
    return this.repository.get(id);
  }

  Single<Lobby> create(Lobby lobby) {
    return this.repository.create(lobby);
  }

  Single<Lobby> update(Lobby lobby) {
    return this.repository.update(lobby);
  }

  Single<Lobby> delete(Integer id) {
    return this.repository.delete(id);
  }
}
