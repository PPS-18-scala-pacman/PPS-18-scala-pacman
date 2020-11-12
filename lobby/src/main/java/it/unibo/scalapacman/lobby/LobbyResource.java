package it.unibo.scalapacman.lobby;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import it.unibo.scalapacman.lobby.util.JsonCollector;
import it.unibo.scalapacman.lobby.util.ResourceUtil;

public class LobbyResource {
  private static final Logger logger = LoggerFactory.getLogger(LobbyResource.class);

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
    this.service.getAll().subscribe(
      lobbies -> {
        final JsonArray array = lobbies.stream()
          .map(Lobby::toJson)
          .collect(JsonCollector.toJsonArray());

        routingContext.response()
          .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
          .setStatusCode(C.HTTP.ResponseCode.OK)
          .end(array.toString());
      },
      ResourceUtil.onError(routingContext)
    );
  }

  private void handleGetById(final RoutingContext routingContext) {
    final Integer id = Integer.valueOf(routingContext.request().getParam("id"));

    this.service.get(id).subscribe(
        result ->
          routingContext.response()
            .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
            .setStatusCode(C.HTTP.ResponseCode.OK)
            .end(result.toJson().toString()),
        ResourceUtil.onError(routingContext)
      );
  }

  private void handleCreate(final RoutingContext routingContext) {
    final Lobby lobby = new Lobby((JsonObject) Json.decodeValue(routingContext.getBodyAsString()));

    this.service.create(lobby).subscribe(
        result ->
          routingContext.response().setStatusCode(C.HTTP.ResponseCode.CREATED)
            .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
            .end(result.toJson().toString()),
        ResourceUtil.onError(routingContext)
      );
  }

  private void handleUpdate(final RoutingContext routingContext) {
//    final Integer id = Integer.valueOf(routingContext.request().getParam("id"));
    final Lobby newLobby = new Lobby((JsonObject) Json.decodeValue(routingContext.getBodyAsString()));

    this.service.update(newLobby).subscribe(
      result ->
        routingContext.response()
          .setStatusCode(C.HTTP.ResponseCode.OK)
          .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
          .end(result.toJson().toString()),
      ResourceUtil.onError(routingContext)
    );
  }

  private void handleDelete(final RoutingContext routingContext) {
    final Integer id = Integer.valueOf(routingContext.request().getParam("id"));

    this.service.delete(id).subscribe(
      result ->
        routingContext.response()
          .setStatusCode(C.HTTP.ResponseCode.OK)
          .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
          .end(result.toJson().toString()),
      ResourceUtil.onError(routingContext)
    );
  }
}
