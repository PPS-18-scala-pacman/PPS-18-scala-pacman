package it.unibo.scalapacman.lobby;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LobbyResource {

  @SuppressWarnings("serial")
  private List<Lobby> lobbies = new ArrayList<Lobby>() {{
    add(new Lobby("L001", "Lobby 1"));
    add(new Lobby("L002", "Lobby 2"));
    add(new Lobby("L003", "Lobby 3"));
  }};

  LobbyResource(Router router) {
    router.get("/api/lobby").handler(this::handleGetAllLobbies);
    router.get("/api/lobby/:id").handler(this::handleGetLobbyById);
    router.post("/api/lobby").handler(this::handleAddLobby);
    router.put("/api/lobby/:id").handler(this::handleUpdateLobby);
    router.delete("/api/lobby/:id").handler(this::handleDeleteLobby);
  }

  private void handleGetAllLobbies(final RoutingContext routingContext) {
    final JsonArray array = new JsonArray();

    lobbies.forEach(lobby -> array.add(lobby.toJson()));

    routingContext.response()
      .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
      .setStatusCode(C.HTTP.ResponseCode.OK)
      .end(array.encodePrettily());
  }

  private void handleGetLobbyById(final RoutingContext routingContext) {
    final String id = routingContext.request().getParam("id");

    final Optional<Lobby> lobby = lobbies.stream().filter(u -> u.getId().contentEquals(id)).findFirst();

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

  private void handleAddLobby(final RoutingContext routingContext) {

    final Lobby lobby = new Lobby((JsonObject) Json.decodeValue(routingContext.getBodyAsString()));

    lobbies.add(lobby);

    routingContext.response().setStatusCode(C.HTTP.ResponseCode.CREATED)
      .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
      .end(Json.encodePrettily(lobby));
  }

  private void handleUpdateLobby(final RoutingContext routingContext) {
    final String id = routingContext.request().getParam("id");

    final Optional<Lobby> lobby = lobbies.stream().filter(u -> u.getId().contentEquals(id)).findFirst();

    if (lobby.isPresent()) {
      final JsonObject newLobby = (JsonObject) Json.decodeValue(routingContext.getBodyAsString());

      lobby.get().setDescription(newLobby.getString("description"));

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
    final String id = routingContext.request().getParam("id");

    final Optional<Lobby> lobby = lobbies.stream().filter(u -> u.getId().contentEquals(id)).findFirst();

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
