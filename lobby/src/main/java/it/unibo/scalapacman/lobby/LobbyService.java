package it.unibo.scalapacman.lobby;

import io.vertx.core.Future;

import java.util.List;
import java.util.Optional;

public class LobbyService {
  private final LobbyRepository repository;

  LobbyService(LobbyRepository repository) {
    this.repository = repository;
  }

  Future<List<Lobby>> getAll() {
    return this.repository.getAll();
  }

  Future<Optional<Lobby>> get(Integer id) {
    return this.repository.get(id);
  }

  Future<Lobby> create(Lobby lobby) {
    return this.repository.create(lobby);
  }

  Future<Lobby> update(Lobby lobby) {
    return this.repository.update(lobby);
  }

  Future<Lobby> delete(Integer id) {
    return this.repository.delete(id);
  }
}
