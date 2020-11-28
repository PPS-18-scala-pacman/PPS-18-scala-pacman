package it.unibo.scalapacman.lobby.service;

import it.unibo.scalapacman.lib.model.PacmanType;
import it.unibo.scalapacman.lobby.dao.Dao;
import it.unibo.scalapacman.lobby.model.Participant;
import it.unibo.scalapacman.lobby.util.REST;
import it.unibo.scalapacman.lobby.util.exception.ConflictException;
import rx.Single;

import java.util.*;
import java.util.stream.Collectors;

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

    return this.usedPacmanType(participant.getLobbyId())
      .map(usedPacmanTypes ->
        // Check if the pacman type is valid, else a not used pacman type is chosen
        Optional.ofNullable(participant.getPacmanType())
          .filter(usedPacmanTypes::contains)
          .orElse(this.usablePacmanType(usedPacmanTypes))
      )
      .doOnSuccess(pacmanType -> {
        if (pacmanType == null) throw new ConflictException("Lobby is full");
      })
      .map(pacmanType -> new Participant(participant.getUsername(), pacmanType, participant.getLobbyId()))
      .flatMap(p -> this.dao.create(p)
          .doOnSuccess(entity -> this.streamService.updateStreams(entity.getLobbyId(), type))
      );
  }

  public Single<Participant> update(String id, Participant participant) {
    LobbyStreamEventType type = new LobbyStreamEventType(LobbyStreamObject.Participant, REST.Update);
    return this.dao.update(id, participant).doOnSuccess(entity -> this.streamService.updateStreams(entity.getLobbyId(), type));
  }

  public Single<Participant> delete(String id) {
    LobbyStreamEventType type = new LobbyStreamEventType(LobbyStreamObject.Participant, REST.Delete);
    return this.dao.delete(id).doOnSuccess(entity -> this.streamService.updateStreams(entity.getLobbyId(), type));
  }

  private Single<List<PacmanType.PacmanType>> usedPacmanType(Long lobbyId) {
    return this.dao.getAll(new Participant(null, (PacmanType.PacmanType) null, lobbyId))
      .map(participants -> participants.stream().map(Participant::getPacmanType).collect(Collectors.toList()));
  }

  private PacmanType.PacmanType usablePacmanType(List<PacmanType.PacmanType> usedTypes) {
    for (int i = 0; i < PacmanType.values().size(); i++) {
      if (!usedTypes.contains(PacmanType.indexToPlayerTypeVal(i))) {
        return PacmanType.indexToPlayerTypeVal(i);
      }
    }
    return null;
  }
}
