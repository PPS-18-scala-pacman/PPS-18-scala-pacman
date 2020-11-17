package it.unibo.scalapacman.lobby;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.pgclient.PgPool;
import io.vertx.rxjava.sqlclient.Row;
import io.vertx.rxjava.sqlclient.Tuple;
import it.unibo.scalapacman.lobby.util.exception.NotFoundException;
import rx.Single;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LobbyRepository {
  private final Logger logger = LoggerFactory.getLogger(LobbyRepository.class);

  private final PgPool dbClient;

  LobbyRepository(PgPool dbClient) {
    this.dbClient = dbClient;
  }

  static Lobby toEntity(Row row) {
    return new Lobby(
      row.getInteger("id"),
      row.getString("description")
    );
  }

  Single<List<Lobby>> getAll() {
    return dbClient
      .query("SELECT * FROM lobby")
      .rxExecute()
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(LobbyRepository::toEntity)
          .collect(Collectors.toList());
      });
  }

  Single<Lobby> get(Integer id) {
    return dbClient
      .preparedQuery("SELECT * FROM lobby WHERE id=$1")
      .rxExecute(Tuple.of(id))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(LobbyRepository::toEntity)
          .findFirst()
          .orElseThrow(NotFoundException::new);
      });
  }

  Single<Lobby> create(Lobby lobby) {
    return dbClient
      .preparedQuery("INSERT INTO lobby (description) VALUES ($1) RETURNING *")
      .rxExecute(Tuple.of(lobby.getDescription()))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(LobbyRepository::toEntity)
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Error: lobby insert failed"));
      });
  }

  Single<Lobby> update(Integer id, Lobby lobby) {
    return dbClient
      .preparedQuery("UPDATE lobby SET description = $2 WHERE id=$1 RETURNING *")
      .rxExecute(Tuple.of(id, lobby.getDescription()))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(LobbyRepository::toEntity)
          .findFirst()
          .orElseThrow(NotFoundException::new);
      });
  }

  Single<Lobby> delete(Integer id) {
    return dbClient
      .preparedQuery("DELETE FROM lobby WHERE id=$1 RETURNING *")
      .rxExecute(Tuple.of(id))
      .map(rows -> {
        logger.debug("Got " + rows.size() + " rows ");
        return StreamSupport.stream(rows.spliterator(), false)
          .map(LobbyRepository::toEntity)
          .findFirst()
          .orElseThrow(NotFoundException::new);
      });
  }
}
