package it.unibo.scalapacman.lobby.dao;

import io.reactivex.Single;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.Tuple;
import it.unibo.scalapacman.lobby.model.Lobby;
import it.unibo.scalapacman.lobby.model.Participant;
import it.unibo.scalapacman.lobby.util.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LobbyDaoImpl implements LobbyDao {
  private final Logger logger = LoggerFactory.getLogger(LobbyDaoImpl.class);

  private final PgPool dbClient;

  public LobbyDaoImpl(PgPool dbClient) {
    this.dbClient = dbClient;
  }

  public static Lobby toEntity(Row row) {
    ArrayList<Participant> participants = new ArrayList<>(1);
    try {
      if (row.getString("username") != null) {
        participants.add(ParticipantDaoImpl.toEntity(row));
      }
    } catch (NoSuchElementException ex) { /* Nothing to do, the column doesn't exists */ }

    return new Lobby(
      row.getLong("lobby_id"),
      row.getString("description"),
      row.getShort("lobby_size"),
      row.getString("host_username"),
      participants
    );
  }

  private static Lobby toEntity(List<Row> rows) {
    if (rows.size() == 0) return null;
    Lobby temp = toEntity(rows.get(0));
    return new Lobby(
      temp.getId(),
      temp.getDescription(),
      temp.getSize(),
      temp.getHostUsername(),
      rows.stream().filter(row -> row.getString("username") != null).map(ParticipantDaoImpl::toEntity).collect(Collectors.toList())
    );
  }

  public Single<List<Lobby>> getAll() {
    return this.getAll(null);
  }

  public Single<List<Lobby>> getAll(Lobby searchParam) {
    String query = "SELECT * FROM lobby LEFT OUTER JOIN participant ON (lobby.lobby_id = participant.lobby_id)";
    Tuple tuple = Tuple.tuple();

    // Search filters
    StringJoiner stringJoiner = new StringJoiner(" AND ", " WHERE ", "").setEmptyValue("");
    if (searchParam != null) {
      if (searchParam.getId() != null) {
        tuple.addValue(searchParam.getId());
        stringJoiner.add("lobby.lobby_id = $" + tuple.size());
      }
      if (searchParam.getDescription() != null) {
        tuple.addValue(searchParam.getDescription());
        stringJoiner.add("lobby.description = $" + tuple.size());
      }
      if (searchParam.getSize() != null) {
        tuple.addValue(searchParam.getSize());
        stringJoiner.add("lobby.size = $" + tuple.size());
      }
      if (searchParam.getHostUsername() != null) {
        tuple.addValue(searchParam.getHostUsername());
        stringJoiner.add("lobby.host_username = $" + tuple.size());
      }
    }
    query += stringJoiner.toString();

    return dbClient
      .preparedQuery(query)
      .rxExecute(tuple)
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .collect(Collectors.groupingBy(LobbyDaoImpl::toEntity));
      })
      .map(hmap ->
        hmap.values().stream()
          .map(LobbyDaoImpl::toEntity)
          .collect(Collectors.toList())
      );
  }

  public Single<Lobby> get(Long id) {
    return dbClient
      .preparedQuery("SELECT * FROM lobby LEFT OUTER JOIN participant ON (lobby.lobby_id = participant.lobby_id) WHERE lobby.lobby_id = $1")
      .rxExecute(Tuple.of(id))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .collect(Collectors.groupingBy(LobbyDaoImpl::toEntity));
      })
      .map(hmap ->
        hmap.values().stream()
          .map(LobbyDaoImpl::toEntity)
          .findFirst()
          .orElseThrow(NotFoundException::new)
      );
  }

  public Single<Lobby> create(Lobby lobby) {
    return dbClient
      .preparedQuery(
        "WITH data(description, lobby_size, username) AS (" +
          "   VALUES ($1, $2::smallint, $3)" +
          ")" +
          ", insert_lobby AS (" +
          "   INSERT INTO lobby (description, lobby_size, host_username)" +
          "   SELECT description, lobby_size, username as host_username" +
          "   FROM   data" +
          "   RETURNING *" +
          ")" +
          ", insert_participant AS (" +
          "   INSERT INTO participant (username, lobby_id)" +
          "   SELECT d.username, l.lobby_id" +
          "   FROM   data d" +
          "   INNER JOIN insert_lobby l ON l.host_username = d.username" +
          "   RETURNING *" +
          ")" +
          " SELECT l.lobby_id, l.description, l.lobby_size, l.host_username, p.username, p.pacman_type" +
          " FROM   insert_participant p" +
          " INNER JOIN insert_lobby l ON l.host_username = p.username"
      )
      .rxExecute(Tuple.of(lobby.getDescription(), lobby.getSize(), lobby.getHostUsername()))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(LobbyDaoImpl::toEntity)
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Error: lobby insert failed"));
      });
  }

  public Single<Lobby> update(Long id, Lobby lobby) {
    return dbClient
      .preparedQuery("UPDATE lobby SET description = $2, lobby_size = $3, host_username = $4 WHERE lobby_id=$1 RETURNING *")
      .rxExecute(Tuple.of(id, lobby.getDescription(), lobby.getSize(), lobby.getHostUsername()))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(LobbyDaoImpl::toEntity)
          .findFirst()
          .orElseThrow(NotFoundException::new);
      });
  }

  public Single<Lobby> delete(Long id) {
    return dbClient
      .preparedQuery("DELETE FROM lobby WHERE lobby_id=$1 RETURNING *")
      .rxExecute(Tuple.of(id))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(LobbyDaoImpl::toEntity)
          .findFirst()
          .orElseThrow(NotFoundException::new);
      });
  }
}
