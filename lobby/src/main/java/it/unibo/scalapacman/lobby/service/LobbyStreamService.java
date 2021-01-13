package it.unibo.scalapacman.lobby.service;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import it.unibo.scalapacman.lobby.dao.LobbyDao;
import it.unibo.scalapacman.lobby.model.Lobby;
import it.unibo.scalapacman.lobby.util.ListJsonable;
import it.unibo.scalapacman.lobby.util.REST;
import it.unibo.scalapacman.lobby.util.SSE;
import it.unibo.scalapacman.lobby.util.exception.APIException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

// https://www.baeldung.com/rxjava-backpressure
// https://www.baeldung.com/rxjava-2-flowable

public class LobbyStreamService {
  private final Logger logger = LoggerFactory.getLogger(LobbyStreamService.class);

  private final static long SAMPLE_MS = 10000;

  private final LobbyDao dao;
  private final PublishSubject<LobbyStreamEventType> eventStream = PublishSubject.create();
  private final PublishSubject<OutputSingle> eventWithDataStream = PublishSubject.create();
  private final Flowable<OutputList> getAllObservable;
  private final ConcurrentMap<Long, Flowable<OutputSingle>> getObservableMap = new ConcurrentHashMap<>();

  public LobbyStreamService(LobbyDao dao) {
    this.dao = dao;

    this.getAllObservable = Flowable.merge(
      this.eventStream.toFlowable(BackpressureStrategy.LATEST),
      this.eventWithDataStream.toFlowable(BackpressureStrategy.LATEST).map(SSE.Event::getType)
    )
      .throttleLatest(1, TimeUnit.SECONDS)
      .switchMap(this::getAllLobbyFromDao)
      .onErrorResumeNext(error -> {
        logger.error(error.getMessage(), error);
        return Flowable.empty(); // Don't close the stream
      })
      .sample(SAMPLE_MS, TimeUnit.MILLISECONDS)
      .replay(1) // Emit the latest value to new subscribers and don't create multiple observable instances
      .refCount(); // Connect to replay with the first subscription

    this.getAllObservable.subscribe(); // Always keep an active connection

    this.eventStream.onNext(new LobbyStreamEventType(LobbyStreamObject.Lobby, REST.Create));
  }

  public Flowable<OutputList> getAllStream() {
    return this.getAllObservable;
  }

  public Flowable<OutputSingle> getByIdStream(Long id) {
    return getObservableMap.get(id);
  }

  public void pushDataToStreams(OutputSingle value) {
    this.eventWithDataStream.onNext(value);
  }

  public void updateStreams(Long lobbyId, LobbyStreamEventType type) {
    this.addToGetObservableMap(lobbyId);
    this.eventStream.onNext(type);
  }

  private void addToGetObservableMap(Long lobbyId) {
    Flowable<OutputSingle> observable = this.getObservableMap.computeIfAbsent(lobbyId, (id) ->
      Flowable.merge(
          eventStream
            .toFlowable(BackpressureStrategy.LATEST)
            .throttleLatest(300, TimeUnit.MILLISECONDS)
            .switchMap(etype -> getLobbyFromDao(etype, lobbyId)),
          eventWithDataStream
            .toFlowable(BackpressureStrategy.LATEST)
        )
        .takeUntil(output -> output.getType().getHttpType().equals(REST.Delete) && (output.getType().getObject().equals(LobbyStreamObject.Lobby) || output.getData() == null))
        .doOnError(error -> {
          logger.error(error.getMessage(), error);
          this.getObservableMap.remove(lobbyId);
        })
        .doOnComplete(() -> this.getObservableMap.remove(lobbyId))
        .replay(1) // Emit the latest value to new subscribers and don't create multiple observable instances
        .refCount() // Connect to replay with the first subscription
    );

    observable.subscribe(); // Always keep an active connection;
  }

  public final Flowable<OutputSingle> getLobbyFromDao(LobbyStreamEventType eventType, Long lobbyId) {
    return this.dao.get(lobbyId)
      .map(entity -> new OutputSingle(eventType, entity))
      .toFlowable()
      .onErrorResumeNext(ex -> {
        if (ex instanceof APIException) {
          logger.info(ex.getMessage());
        } else {
          logger.error(ex.getMessage(), ex);
        }
        return Flowable.empty(); // Don't close the stream
      });
  }

  public final Flowable<OutputList> getAllLobbyFromDao(LobbyStreamEventType eventType) {
    return this.dao.getAll()
      .map(entities -> new OutputList(eventType, new ListJsonable<>(entities)))
      .toFlowable()
      .onErrorResumeNext(ex -> {
        if (ex instanceof APIException) {
          logger.info(ex.getMessage());
        } else {
          logger.error(ex.getMessage(), ex);
        }
        return Flowable.empty(); // Don't close the stream
      });
  }

  public static class OutputSingle extends SSE.Event<LobbyStreamEventType, Lobby> {
    public OutputSingle(LobbyStreamEventType type, Lobby data) {
      super(type, data);
    }
  }

  public static class OutputList extends SSE.Event<LobbyStreamEventType, ListJsonable<Lobby>> {
    public OutputList(LobbyStreamEventType type, ListJsonable<Lobby> data) {
      super(type, data);
    }
  }
}
