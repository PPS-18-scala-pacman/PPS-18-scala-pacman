package it.unibo.scalapacman.lobby;

import rx.Observable;
import rx.Single;
import rx.subjects.BehaviorSubject;

import java.util.*;

public class LobbyService {
  private final LobbyRepository repository;
  private final BehaviorSubject<List<Lobby>> getAllSubject = BehaviorSubject.create(new ArrayList<>());
  private final Map<Integer, BehaviorSubject<Lobby>> getByIdSubject = new HashMap<>();

  public LobbyService(LobbyRepository repository) {
    this.repository = repository;
    this.initStreams();
  }

  private void initStreams() {
    this.getAll().subscribe(getAllSubject::onNext);
  }

  public Single<List<Lobby>> getAll() {
    return this.repository.getAll();
  }

  public Observable<List<Lobby>> getAllStream() {
    return getAllSubject;
  }

  public Single<Lobby> get(Integer id) {
    return this.repository.get(id);
  }

  public Observable<Lobby> getStream(Integer id) {
    if (!getByIdSubject.containsKey(id)) {
      BehaviorSubject<Lobby> subject = BehaviorSubject.create();
      this.get(id).subscribe(subject::onNext);
      getByIdSubject.put(id, subject);
    }

    return getByIdSubject.get(id);
  }

  public Single<Lobby> create(Lobby lobby) {
    return this.repository.create(lobby).doOnSuccess(this::updateStreams);
  }

  public Single<Lobby> update(Integer id, Lobby lobby) {
    return this.repository.update(id, lobby).doOnSuccess(this::updateStreams);
  }

  public Single<Lobby> delete(Integer id) {
    return this.repository.delete(id).doOnSuccess(entity -> updateStreams(entity, true));
  }

  private void updateStreams(Lobby entity) {
    this.updateStreams(entity, false);
  }

  private void updateStreams(Lobby entity, boolean delete) {
    this.repository.getAll().subscribe(getAllSubject::onNext);
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
