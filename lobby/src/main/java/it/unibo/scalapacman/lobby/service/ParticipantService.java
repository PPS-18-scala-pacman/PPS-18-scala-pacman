package it.unibo.scalapacman.lobby.service;

import it.unibo.scalapacman.lobby.dao.Dao;
import it.unibo.scalapacman.lobby.model.Participant;
import rx.Single;

import java.util.*;

public class ParticipantService {
  private final Dao<Participant, String> repository;
  private final LobbyStreamService streamService;

  public ParticipantService(final Dao<Participant, String> repository, final LobbyStreamService streamService) {
    this.repository = repository;
    this.streamService = streamService;
  }

  public Single<List<Participant>> getAll() {
    return this.repository.getAll();
  }

  public Single<Participant> get(String id) {
    return this.repository.get(id);
  }

  public Single<Participant> create(Participant participant) {
    return this.repository.create(participant).doOnSuccess(entity -> this.streamService.updateStreams(entity.getLobbyId()));
  }

  public Single<Participant> update(String id, Participant participant) {
    return this.repository.update(id, participant).doOnSuccess(entity -> this.streamService.updateStreams(entity.getLobbyId()));
  }

  public Single<Participant> delete(String id) {
    return this.repository.delete(id).doOnSuccess(entity -> this.streamService.updateStreams(entity.getLobbyId()));
  }
}
