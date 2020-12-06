package it.unibo.scalapacman.lobby.service;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import it.unibo.scalapacman.lobby.dao.LobbyDao;
import it.unibo.scalapacman.lobby.model.Lobby;
import it.unibo.scalapacman.lobby.util.ListJsonable;
import it.unibo.scalapacman.lobby.util.REST;
import it.unibo.scalapacman.lobby.util.SSE;
import it.unibo.scalapacman.lobby.util.exception.NotFoundException;
import rx.BackpressureOverflow;
import rx.Observable;
import rx.Single;
import rx.subjects.BehaviorSubject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

// https://www.baeldung.com/rxjava-backpressure

public class LobbyStreamService {
  private final Logger logger = LoggerFactory.getLogger(LobbyStreamService.class);

  private final static long SLOW_SAMPLE_MS = 10000;
  private final static long MEDIUM_SAMPLE_MS = 1000;

  private final LobbyDao dao;
  private final BehaviorSubject<SSE.Event<LobbyStreamEventType, ListJsonable<Lobby>>> getAllSubject = BehaviorSubject.create();
  private final Map<Long, BehaviorSubject<SSE.Event<LobbyStreamEventType, Lobby>>> getByIdSubject = new HashMap<>();

  public LobbyStreamService(LobbyDao dao) {
    this.dao = dao;
    this.initStreams();
  }

  private void initStreams() {
    this.updateStreamAll(new LobbyStreamEventType(LobbyStreamObject.Lobby, REST.Create));
  }

  public Observable<SSE.Event<LobbyStreamEventType, ListJsonable<Lobby>>> getAllStream() {
    return getAllSubject
      .onBackpressureBuffer(1, () -> {}, BackpressureOverflow.ON_OVERFLOW_DROP_OLDEST)
      .sample(SLOW_SAMPLE_MS, TimeUnit.MILLISECONDS)
      .startWith(getAllSubject.getValue());
  }

  public Observable<SSE.Event<LobbyStreamEventType, Lobby>> getByIdStream(Long id) {
    BehaviorSubject<SSE.Event<LobbyStreamEventType, Lobby>> subject = null;
    Single<SSE.Event<LobbyStreamEventType, Lobby>> firstValue = null;

    if (getByIdSubject.containsKey(id)) {
      subject = getByIdSubject.get(id);
      firstValue = Single.just(subject.getValue());
    } else {
      // Recovering the first value
      LobbyStreamEventType type = new LobbyStreamEventType(LobbyStreamObject.Lobby, REST.Create);
      firstValue = this.dao.get(id)
        .map(entity -> new SSE.Event<>(type, entity));

      // Initializing the subject
      subject = BehaviorSubject.create();
      firstValue.subscribe(subject::onNext, this::onError);
      getByIdSubject.put(id, subject);
    }

    return subject
      .onBackpressureBuffer(1, () -> {}, BackpressureOverflow.ON_OVERFLOW_DROP_OLDEST)
      .startWith(firstValue.toObservable());
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

        boolean lobbyIsDeleted = type.getHttpType().equals(REST.Delete) && (type.getObject().equals(LobbyStreamObject.Lobby) || lobby == null);
        if (lobbyIsDeleted) {
          subject.onCompleted();
          this.getByIdSubject.remove(lobbyId);
        }
      });
  }

  private void onError(Throwable ex) {
    this.logger.error(ex.getMessage(), ex);
  }
}
