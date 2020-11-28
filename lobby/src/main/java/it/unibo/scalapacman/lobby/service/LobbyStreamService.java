package it.unibo.scalapacman.lobby.service;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import it.unibo.scalapacman.lobby.dao.Dao;
import it.unibo.scalapacman.lobby.model.Lobby;
import it.unibo.scalapacman.lobby.util.ListJsonable;
import it.unibo.scalapacman.lobby.util.REST;
import it.unibo.scalapacman.lobby.util.SSE;
import it.unibo.scalapacman.lobby.util.exception.NotFoundException;
import rx.Observable;
import rx.Single;
import rx.subjects.BehaviorSubject;

import java.util.*;

public class LobbyStreamService {
  private final Logger logger = LoggerFactory.getLogger(LobbyStreamService.class);

  private final Dao<Lobby, Long> dao;
  private final BehaviorSubject<SSE.Event<LobbyStreamEventType, ListJsonable<Lobby>>> getAllSubject = BehaviorSubject.create();
  private final Map<Long, BehaviorSubject<SSE.Event<LobbyStreamEventType, Lobby>>> getByIdSubject = new HashMap<>();

  public LobbyStreamService(Dao<Lobby, Long> dao) {
    this.dao = dao;
    this.initStreams();
  }

  private void initStreams() {
    this.updateStreamAll(new LobbyStreamEventType(LobbyStreamObject.Lobby, REST.Create));
  }

  public Observable<SSE.Event<LobbyStreamEventType, ListJsonable<Lobby>>> getStreamAll() {
    return getAllSubject;
  }

  public Observable<SSE.Event<LobbyStreamEventType, Lobby>> getStreamById(Long id) {
    LobbyStreamEventType type = new LobbyStreamEventType(LobbyStreamObject.Lobby, REST.Create);
    if (!getByIdSubject.containsKey(id)) {
      BehaviorSubject<SSE.Event<LobbyStreamEventType, Lobby>> subject = BehaviorSubject.create();
      this.dao.get(id)
        .map(entity -> new SSE.Event<>(type, entity))
        .subscribe(subject::onNext, this::onError);
      getByIdSubject.put(id, subject);
    }

    return getByIdSubject.get(id);
  }

  public void updateStreams(Long lobbyId, LobbyStreamEventType type) {
    this.dao.get(lobbyId)
      .onErrorResumeNext(err -> {
        if (type.getHttpType().equals(REST.Delete) && err instanceof NotFoundException) {
          return Single.just(null);
        }
        return Single.error(err);
      })
      .subscribe(lobby -> this.updateStreams(lobbyId, lobby, type), this::onError);
  }

  public void updateStreams(Long lobbyId, Lobby lobby, LobbyStreamEventType type) {
    this.updateStreamAll(type);
    this.updateStreamById(lobbyId, lobby, type);
  }

  private void updateStreamAll(LobbyStreamEventType type) {
    this.dao.getAll()
      .map(entities -> new SSE.Event<>(type, new ListJsonable<>(entities)))
      .subscribe(getAllSubject::onNext, this::onError);
  }

  private void updateStreamById(Long lobbyId, Lobby lobby, LobbyStreamEventType type) {
    SSE.Event<LobbyStreamEventType, Lobby> event = new SSE.Event<>(type, lobby);
    Optional.ofNullable(this.getByIdSubject.get(lobbyId))
      .ifPresent(subject -> {
        subject.onNext(event);

        if (type.getHttpType().equals(REST.Delete)) {
          subject.onCompleted();
          this.getByIdSubject.remove(lobbyId);
        }
      });
  }

  private void onError(Throwable ex) {
    this.logger.error(ex.getMessage(), ex);
  }
}
