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
      row.getInteger("lobby_size")
    );
  }

  private static Lobby toEntity(List<Row> rows) {
    if (rows.size() == 0) return null;
    return new Lobby(
      rows.get(0).getLong("lobby_id"),
      rows.get(0).getString("description"),
      rows.get(0).getInteger("lobby_size"),
      rows.stream().map(ParticipantDao::toEntity).collect(Collectors.toList())
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
      .preparedQuery("SELECT * FROM lobby WHERE id=$1 LEFT OUTER JOIN participant ON (lobby.lobby_id = participant.lobby_id)")
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
      .preparedQuery("INSERT INTO lobby (description, lobby_size) VALUES ($1, $2) RETURNING *")
      .rxExecute(Tuple.of(lobby.getDescription(), lobby.getSize()))
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
      .preparedQuery("UPDATE lobby SET description = $2, lobby_size = $3 WHERE lobby_id=$1 RETURNING *")
      .rxExecute(Tuple.of(id, lobby.getDescription(), lobby.getSize()))
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
