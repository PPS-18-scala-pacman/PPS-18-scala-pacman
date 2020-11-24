package it.unibo.scalapacman.lobby.service;

import it.unibo.scalapacman.lobby.dao.Dao;
import it.unibo.scalapacman.lobby.model.Lobby;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.util.*;

public class LobbyStreamService {
  private final Dao<Lobby, Long> dao;
  private final BehaviorSubject<List<Lobby>> getAllSubject = BehaviorSubject.create(new ArrayList<>());
  private final Map<Long, BehaviorSubject<Lobby>> getByIdSubject = new HashMap<>();

  public LobbyStreamService(Dao<Lobby, Long> dao) {
    this.dao = dao;
    this.initStreams();
  }

  private void initStreams() {
    this.updateStreamAll();
  }

  public Observable<List<Lobby>> getStreamAll() {
    return getAllSubject;
  }

  public Observable<Lobby> getStreamById(Long id) {
    if (!getByIdSubject.containsKey(id)) {
      BehaviorSubject<Lobby> subject = BehaviorSubject.create();
      this.dao.get(id).subscribe(subject::onNext);
      getByIdSubject.put(id, subject);
    }

    return getByIdSubject.get(id);
  }

  public void updateStreams(Long entityId) {
    this.updateStreams(entityId, false);
  }
  public void updateStreams(Long entityId, boolean delete) {
    this.dao.get(entityId).subscribe(entity -> this.updateStreams(entity, delete));
  }

  public void updateStreams(Lobby entity) {
    this.updateStreams(entity, false);
  }

  public void updateStreams(Lobby entity, boolean delete) {
    this.updateStreamAll();
    this.updateStreamById(entity, delete);
  }

  private void updateStreamAll() {
    this.dao.getAll().subscribe(getAllSubject::onNext);
  }

  private void updateStreamById(Lobby entity, boolean delete) {
    if (delete) {
      Optional.ofNullable(this.getByIdSubject.get(entity.getId())).ifPresent(subject -> {
        subject.onNext(null);
        subject.onCompleted();
      });
      this.getByIdSubject.remove(entity.getId());
    } else {
      Optional.ofNullable(this.getByIdSubject.get(entity.getId())).ifPresent(subject -> subject.onNext(entity));
    }
  }
}
