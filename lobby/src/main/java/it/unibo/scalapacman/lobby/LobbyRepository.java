package it.unibo.scalapacman.lobby;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LobbyRepository {
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

  Future<List<Lobby>> getAll() {
    Promise<List<Lobby>> promise = Promise.promise();
    dbClient
      .query("SELECT * FROM lobby")
      .execute(ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          System.out.println("Got " + rows.size() + " rows ");
          List<Lobby> entities = StreamSupport.stream(rows.spliterator(), false)
            .map(LobbyRepository::toEntity)
            .collect(Collectors.toList());
          promise.complete(entities);
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
          promise.fail(ar.cause());
        }
      });
    return promise.future();
  }

  Future<Lobby> get(Integer id) {
    Promise<Lobby> promise = Promise.promise();
    dbClient
      .preparedQuery("SELECT * FROM lobby WHERE id=$1")
      .execute(Tuple.of(id), ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          System.out.println("Got " + rows.size() + " rows ");
          List<Lobby> entities = StreamSupport.stream(rows.spliterator(), false)
            .map(LobbyRepository::toEntity)
            .collect(Collectors.toList());
          promise.complete(entities.get(0));
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
    return promise.future();
  }

  Future<Lobby> create(Lobby lobby) {
    Promise<Lobby> promise = Promise.promise();
    dbClient
      .preparedQuery("INSERT INTO lobby (description) VALUES ($1) RETURNING id, description")
      .execute(Tuple.of(lobby.getDescription()), ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          System.out.println("Got " + rows.size() + " rows ");
          List<Lobby> entities = StreamSupport.stream(rows.spliterator(), false)
            .map(LobbyRepository::toEntity)
            .collect(Collectors.toList());
          promise.complete(entities.get(0));
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
    return promise.future();
  }

  Future<Lobby> update(Lobby lobby) {
    Promise<Lobby> promise = Promise.promise();
    dbClient
      .preparedQuery("UPDATE lobby SET description = $2 FROM lobby WHERE id=$1")
      .execute(Tuple.of(lobby.getId(), lobby.getDescription()), ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          System.out.println("Got " + rows.size() + " rows ");
          promise.complete(lobby);
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
    return promise.future();
  }

  Future<Lobby> delete(Integer id) {
    Promise<Lobby> promise = Promise.promise();
    dbClient
      .preparedQuery("DELETE FROM lobby WHERE id=$1")
      .execute(Tuple.of(id), ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          System.out.println("Got " + rows.size() + " rows ");
          List<Lobby> entities = StreamSupport.stream(rows.spliterator(), false)
            .map(LobbyRepository::toEntity)
            .collect(Collectors.toList());
          promise.complete(entities.get(0));
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
    return promise.future();
  }
}
