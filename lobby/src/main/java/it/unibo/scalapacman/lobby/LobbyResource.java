package it.unibo.scalapacman.lobby;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import it.unibo.scalapacman.lobby.util.JsonCollector;

import java.util.Optional;

public class LobbyResource {

  private final LobbyService service;

  LobbyResource(Router router, LobbyService service) {
    this.service = service;

    router.get("/api/lobby").handler(this::handleGetAll);
    router.get("/api/lobby/:id").handler(this::handleGetById);
    router.post("/api/lobby").handler(this::handleCreate);
    router.put("/api/lobby/:id").handler(this::handleUpdate);
    router.delete("/api/lobby/:id").handler(this::handleDelete);
  }

  private void handleGetAll(final RoutingContext routingContext) {
    this.service.getAll()
      .onComplete(lobbies -> {
        if (lobbies.failed()) {
          routingContext.fail(lobbies.cause());
        } else {
          final JsonArray array = lobbies.result().stream()
            .map(Lobby::toJson)
            .collect(JsonCollector.toJsonArray());

          routingContext.response()
            .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
            .setStatusCode(C.HTTP.ResponseCode.OK)
            .end(array.toString());
        }
      });
  }

  private void handleGetById(final RoutingContext routingContext) {
    final Integer id = Integer.valueOf(routingContext.request().getParam("id"));

    this.service.get(id)
      .onComplete(ar -> {
        if (ar.failed()) {
          routingContext.fail(ar.cause());
        } else {
          final Optional<Lobby> result = ar.result();

          if (result.isPresent()) {
            routingContext.response()
              .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
              .setStatusCode(C.HTTP.ResponseCode.OK)
              .end(result.get().toJson().toString());
          } else {
            routingContext.response()
              .setStatusCode(C.HTTP.ResponseCode.NOT_FOUND)
              .end();
          }
        }
      });
  }

  private void handleCreate(final RoutingContext routingContext) {

    final Lobby lobby = new Lobby((JsonObject) Json.decodeValue(routingContext.getBodyAsString()));

    this.service.create(lobby)
      .onComplete(ar -> {
        if (ar.failed()) {
          routingContext.fail(ar.cause());
        } else {
          final Lobby result = ar.result();

          routingContext.response().setStatusCode(C.HTTP.ResponseCode.CREATED)
            .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
            .end(result.toJson().toString());
        }
      });
  }

  private void handleUpdate(final RoutingContext routingContext) {
//    final Integer id = Integer.valueOf(routingContext.request().getParam("id"));
    final Lobby newLobby = new Lobby((JsonObject) Json.decodeValue(routingContext.getBodyAsString()));

    // TODO verificare prima se non esiste, nel qual caso ritornare 404

    this.service.update(newLobby)
      .onComplete(ar -> {
        if (ar.failed()) {
          routingContext.fail(ar.cause());
        } else {
          final Lobby result = ar.result();

          routingContext.response()
            .setStatusCode(C.HTTP.ResponseCode.OK)
            .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
            .end(result.toJson().toString());
        }
      });
  }

  private void handleDelete(final RoutingContext routingContext) {
    final Integer id = Integer.valueOf(routingContext.request().getParam("id"));

    // TODO verificare prima se non esiste, nel qual caso ritornare 404

    this.service.delete(id)
      .onComplete(ar -> {
        if (ar.failed()) {
          routingContext.fail(ar.cause());
        } else {
          final Lobby result = ar.result();

          routingContext.response()
            .setStatusCode(C.HTTP.ResponseCode.OK)
            .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
            .end(result.toJson().toString());
        }
      });
  }
}
