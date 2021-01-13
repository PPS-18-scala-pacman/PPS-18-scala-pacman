package it.unibo.scalapacman.lobby.dao;

import io.reactivex.Single;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.Tuple;
import it.unibo.scalapacman.lobby.model.Participant;
import it.unibo.scalapacman.lobby.util.exception.NotFoundException;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ParticipantDaoImpl implements ParticipantDao {
  private final Logger logger = LoggerFactory.getLogger(ParticipantDaoImpl.class);

  private final PgPool dbClient;

  public ParticipantDaoImpl(PgPool dbClient) {
    this.dbClient = dbClient;
  }

  public static Participant toEntity(Row row) {
    return new Participant(
      row.getString("username"),
      row.getInteger("pacman_type"),
      row.getLong("lobby_id")
    );
  }

  public Single<List<Participant>> getAll() {
    return this.getAll(null);
  }

  public Single<List<Participant>> getAll(Participant searchParam) {
    String query = "SELECT * FROM participant";
    Tuple tuple = Tuple.tuple();

    // Search filters
    StringJoiner stringJoiner = new StringJoiner(" AND ", " WHERE ", "").setEmptyValue("");
    if (searchParam != null) {
      if (searchParam.getUsername() != null) {
        tuple.addValue(searchParam.getUsername());
        stringJoiner.add("username = $" + tuple.size());
      }
      if (searchParam.getPacmanTypeAsInteger() != null) {
        tuple.addValue(searchParam.getPacmanTypeAsInteger());
        stringJoiner.add("pacman_type = $" + tuple.size());
      }
      if (searchParam.getLobbyId() != null) {
        tuple.addValue(searchParam.getLobbyId());
        stringJoiner.add("lobby_id = $" + tuple.size());
      }
    }
    query += stringJoiner.toString();

    return dbClient
      .preparedQuery(query)
      .rxExecute(tuple)
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(ParticipantDaoImpl::toEntity)
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
          .map(ParticipantDaoImpl::toEntity)
          .findFirst()
          .orElseThrow(NotFoundException::new);
      });
  }

  public Single<Participant> create(Participant participant) {
    return dbClient
      .preparedQuery("INSERT INTO participant (username, pacman_type, lobby_id) VALUES ($1, $2, $3) RETURNING *")
      .rxExecute(Tuple.of(participant.getUsername(), participant.getPacmanTypeAsInteger(), participant.getLobbyId()))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(ParticipantDaoImpl::toEntity)
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Error: participant insert failed"));
      });
  }

  public Single<Participant> update(String username, Participant participant) {
    return dbClient
      .preparedQuery("UPDATE participant SET pacman_type = $2 WHERE username=$1 RETURNING *")
      .rxExecute(Tuple.of(username, participant.getPacmanTypeAsInteger()))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(ParticipantDaoImpl::toEntity)
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
          .map(ParticipantDaoImpl::toEntity)
          .findFirst()
          .orElseThrow(NotFoundException::new);
      });
  }
}
