package it.unibo.scalapacman.lobby.service;

import it.unibo.scalapacman.lobby.dao.Dao;
import it.unibo.scalapacman.lobby.model.Participant;
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
    return this.dao.create(participant).doOnSuccess(entity -> this.streamService.updateStreams(entity.getLobbyId()));
  }

  public Single<Participant> update(String id, Participant participant) {
    return this.dao.update(id, participant).doOnSuccess(entity -> this.streamService.updateStreams(entity.getLobbyId()));
  }

  public Single<Participant> delete(String id) {
    return this.dao.delete(id).doOnSuccess(entity -> this.streamService.updateStreams(entity.getLobbyId()));
  }
}
