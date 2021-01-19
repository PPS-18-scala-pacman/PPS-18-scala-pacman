package it.unibo.scalapacman.lobby.service;

import io.reactivex.Single;
import it.unibo.scalapacman.lobby.dao.LobbyDao;
import it.unibo.scalapacman.lobby.model.Lobby;
import it.unibo.scalapacman.lobby.model.Participant;
import it.unibo.scalapacman.lobby.util.REST;

import java.util.*;

public class LobbyService {
  private final LobbyDao dao;
  private final LobbyStreamService streamService;

  public LobbyService(final LobbyDao dao, final LobbyStreamService streamService) {
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
    Participant host = new Participant(lobby.getHostUsername(), 0, lobby.getId());
    lobby.setParticipants(Collections.singletonList(host));

    LobbyStreamEventType type = new LobbyStreamEventType(LobbyStreamObject.Lobby, REST.Create);
    return this.dao.create(lobby).doOnSuccess(entity -> this.streamService.updateStreams(entity.getId(), type));
  }

  public Single<Lobby> update(Long id, Lobby lobby) {
    LobbyStreamEventType type = new LobbyStreamEventType(LobbyStreamObject.Lobby, REST.Update);
    return this.dao.update(id, lobby).doOnSuccess(entity -> this.streamService.updateStreams(id, type));
  }

  public Single<Lobby> delete(Long id) {
    LobbyStreamEventType type = new LobbyStreamEventType(LobbyStreamObject.Lobby, REST.Delete);
    return this.dao.delete(id).doOnSuccess(entity -> this.streamService.updateStreams(id, type));
  }
}
