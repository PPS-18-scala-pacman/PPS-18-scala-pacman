package it.unibo.scalapacman.lobby.dao;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.pgclient.PgPool;
import io.vertx.rxjava.sqlclient.Row;
import io.vertx.rxjava.sqlclient.Tuple;
import it.unibo.scalapacman.lobby.model.Lobby;
import it.unibo.scalapacman.lobby.util.exception.NotFoundException;
import rx.Single;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LobbyDao implements Dao<Lobby, Long> {
  private final Logger logger = LoggerFactory.getLogger(LobbyDao.class);

  private final PgPool dbClient;

  public LobbyDao(PgPool dbClient) {
    this.dbClient = dbClient;
  }

  public static Lobby toEntity(Row row) {
    return new Lobby(
      row.getLong("lobby_id"),
      row.getString("description"),
      row.getInteger("lobby_size"),
      row.getString("host_username")
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
      rows.stream().filter(row -> row.getString("username") != null).map(ParticipantDao::toEntity).collect(Collectors.toList())
    );
  }

  public Single<List<Lobby>> getAll() {
    return dbClient
      .query("SELECT * FROM lobby LEFT OUTER JOIN participant ON (lobby.lobby_id = participant.lobby_id)")
      .rxExecute()
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .collect(Collectors.groupingBy(LobbyDao::toEntity));
      })
      .map(hmap ->
        hmap.values().stream()
          .map(LobbyDao::toEntity)
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
          .collect(Collectors.groupingBy(LobbyDao::toEntity));
      })
      .map(hmap ->
        hmap.values().stream()
          .map(LobbyDao::toEntity)
          .findFirst()
          .orElseThrow(NotFoundException::new)
      );
  }

  public Single<Lobby> create(Lobby lobby) {
    return dbClient
      .preparedQuery(
        "WITH data(description, lobby_size, username) AS (" +
          "   VALUES ($1, $2, $3)" +
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
          "SELECT l.lobby_id, l.description, l.lobby_size, l.host_username, p.username, p.pacman_type" +
          "FROM   insert_participant p" +
          "INNER JOIN insert_lobby l ON l.host_username = p.username"
      )
      .rxExecute(Tuple.of(lobby.getDescription(), lobby.getSize(), lobby.getHostUsername()))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(LobbyDao::toEntity)
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
          .map(LobbyDao::toEntity)
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
          .map(LobbyDao::toEntity)
          .findFirst()
          .orElseThrow(NotFoundException::new);
      });
  }
}
