package it.unibo.scalapacman.lobby.service;

import it.unibo.scalapacman.lobby.dao.Dao;
import it.unibo.scalapacman.lobby.model.Participant;
import it.unibo.scalapacman.lobby.util.REST;
import rx.Single;

import java.util.*;

public class ParticipantService {
  private final Dao<Participant, String> dao;
  private final LobbyStreamService streamService;

  public ParticipantService(final Dao<Participant, String> dao, final LobbyStreamService streamService) {
    this.dao = dao;
    this.streamService = streamService;
  }

  public Single<List<Participant>> getAll() {
    return this.dao.getAll();
  }

  public Single<Participant> get(String id) {
    return this.dao.get(id);
  }

  public Single<Participant> create(Participant participant) {
    LobbyStreamEventType type = new LobbyStreamEventType(LobbyStreamObject.Participant, REST.Create);
    return this.dao.create(participant).doOnSuccess(entity -> this.streamService.updateStreams(entity.getLobbyId(), type));
  }

  public Single<Participant> update(String id, Participant participant) {
    LobbyStreamEventType type = new LobbyStreamEventType(LobbyStreamObject.Participant, REST.Update);
    return this.dao.update(id, participant).doOnSuccess(entity -> this.streamService.updateStreams(entity.getLobbyId(), type));
  }

  public Single<Participant> delete(String id) {
    LobbyStreamEventType type = new LobbyStreamEventType(LobbyStreamObject.Participant, REST.Delete);
    return this.dao.delete(id).doOnSuccess(entity -> this.streamService.updateStreams(entity.getLobbyId(), type));
  }
}
