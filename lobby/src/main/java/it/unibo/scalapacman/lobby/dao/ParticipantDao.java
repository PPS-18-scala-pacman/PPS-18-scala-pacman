package it.unibo.scalapacman.lobby.dao;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.pgclient.PgPool;
import io.vertx.rxjava.sqlclient.Row;
import io.vertx.rxjava.sqlclient.Tuple;
import it.unibo.scalapacman.lobby.model.Participant;
import it.unibo.scalapacman.lobby.util.exception.NotFoundException;
import rx.Single;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ParticipantDao implements Dao<Participant, String> {
  private final Logger logger = LoggerFactory.getLogger(ParticipantDao.class);

  private final PgPool dbClient;

  public ParticipantDao(PgPool dbClient) {
    this.dbClient = dbClient;
  }

  public static Participant toEntity(Row row) {
    return new Participant(
      row.getString("username"),
      row.getBoolean("host"),
      row.getInteger("pacman_type"),
      row.getLong("lobby_id")
    );
  }

  public Single<List<Participant>> getAll() {
    return dbClient
      .query("SELECT * FROM participant")
      .rxExecute()
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(ParticipantDao::toEntity)
          .collect(Collectors.toList());
      });
  }

  public Single<Participant> get(String username) {
    return dbClient
      .preparedQuery("SELECT * FROM participant WHERE username=$1")
      .rxExecute(Tuple.of(username))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(ParticipantDao::toEntity)
          .findFirst()
          .orElseThrow(NotFoundException::new);
      });
  }

  public Single<Participant> create(Participant participant) {
    return dbClient
      .preparedQuery("INSERT INTO participant (username, host, pacman_type, lobby_id) VALUES ($1, $2, $3, $4) RETURNING *")
      .rxExecute(Tuple.of(participant.getUsername(), participant.getHost(), participant.getPacmanTypeAsInteger(), participant.getLobbyId()))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(ParticipantDao::toEntity)
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Error: participant insert failed"));
      });
  }

  public Single<Participant> update(String username, Participant participant) {
    return dbClient
      .preparedQuery("UPDATE participant SET host = $2, pacman_type = $3 WHERE username=$1 RETURNING *")
      .rxExecute(Tuple.of(username, participant.getHost(), participant.getPacmanTypeAsInteger()))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(ParticipantDao::toEntity)
          .findFirst()
          .orElseThrow(NotFoundException::new);
      });
  }

  public Single<Participant> delete(String username) {
    return dbClient
      .preparedQuery("DELETE FROM participant WHERE username=$1 RETURNING *")
      .rxExecute(Tuple.of(username))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(ParticipantDao::toEntity)
          .findFirst()
          .orElseThrow(NotFoundException::new);
      });
  }
}
