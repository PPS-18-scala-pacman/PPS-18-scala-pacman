package it.unibo.scalapacman.lobby;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

public class LobbyResource {

  private final LobbyService service;

  LobbyResource(Router router, LobbyService service) {
    this.service = service;

    router.get("/api/lobby").handler(this::handleGetAllLobbies);
    router.get("/api/lobby/:id").handler(this::handleGetLobbyById);
    router.post("/api/lobby").handler(this::handleCreateLobby);
    router.put("/api/lobby/:id").handler(this::handleUpdateLobby);
    router.delete("/api/lobby/:id").handler(this::handleDeleteLobby);
  }

  private void handleGetAllLobbies(final RoutingContext routingContext) {
    final JsonArray array = new JsonArray();

    this.service.getAll();

    routingContext.response()
      .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
      .setStatusCode(C.HTTP.ResponseCode.OK)
      .end(array.encodePrettily());
  }

  private void handleGetLobbyById(final RoutingContext routingContext) {
    final Integer id = Integer.valueOf(routingContext.request().getParam("id"));

    final Optional<Lobby> lobby = lobbies.stream().filter(u -> u.getId().equals(id)).findFirst();

    if (lobby.isPresent()) {
      routingContext.response()
        .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
        .setStatusCode(C.HTTP.ResponseCode.OK)
        .end(lobby.get().toString());
    } else {
      routingContext.response()
        .setStatusCode(C.HTTP.ResponseCode.NOT_FOUND)
        .end();
    }
  }

  private void handleCreateLobby(final RoutingContext routingContext) {

    final Lobby lobby = new Lobby((JsonObject) Json.decodeValue(routingContext.getBodyAsString()));

    this.service.create(lobby);

    routingContext.response().setStatusCode(C.HTTP.ResponseCode.CREATED)
      .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
      .end(Json.encodePrettily(lobby));
  }

  private void handleUpdateLobby(final RoutingContext routingContext) {
    final Integer id = Integer.valueOf(routingContext.request().getParam("id"));

    // FIXME
    if (lobby.isPresent()) {
      final Lobby newLobby = new Lobby((JsonObject) Json.decodeValue(routingContext.getBodyAsString()));

      this.service.update(newLobby);

      routingContext.response()
        .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
        .setStatusCode(C.HTTP.ResponseCode.OK).end(lobby.get().toString());

    } else {
      routingContext.response()
        .setStatusCode(C.HTTP.ResponseCode.NOT_FOUND)
        .end();
    }
  }

  private void handleDeleteLobby(final RoutingContext routingContext) {
    final Integer id = Integer.valueOf(routingContext.request().getParam("id"));

    this.service.delete(id);

    // FIXME
    if (lobby.isPresent()) {
      lobbies.remove(lobby.get());

      routingContext.response()
        .setStatusCode(C.HTTP.ResponseCode.OK)
        .end();

    } else {
      routingContext.response()
        .setStatusCode(C.HTTP.ResponseCode.NOT_FOUND)
        .end();
    }
  }
}
