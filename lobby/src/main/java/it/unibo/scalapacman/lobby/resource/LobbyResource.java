package it.unibo.scalapacman.lobby.resource;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import it.unibo.scalapacman.lobby.C;
import it.unibo.scalapacman.lobby.model.Lobby;
import it.unibo.scalapacman.lobby.service.LobbyService;
import it.unibo.scalapacman.lobby.service.LobbyStreamEventType;
import it.unibo.scalapacman.lobby.service.LobbyStreamService;
import it.unibo.scalapacman.lobby.util.JsonCollector;
import it.unibo.scalapacman.lobby.util.VertxUtil;
import it.unibo.scalapacman.lobby.util.SSE;

import java.util.Optional;

public class LobbyResource {
  private static final Logger logger = LoggerFactory.getLogger(LobbyResource.class);

  private final LobbyService service;
  private final LobbyStreamService streamService;

  public LobbyResource(Router router, LobbyService service, LobbyStreamService streamService) {
    this.service = service;
    this.streamService = streamService;

    router.get("/api/lobby").produces("application/json").handler(this::handleGetAll);
    router.get("/api/lobby").produces("text/event-stream").handler(this::handleGetAllStream);
    router.get("/api/lobby/:id").produces("application/json").handler(this::handleGetById);
    router.get("/api/lobby/:id").produces("text/event-stream").handler(this::handleGetByIdStream);
    router.post("/api/lobby").produces("application/json").handler(this::handleCreate);
    router.put("/api/lobby/:id").produces("application/json").handler(this::handleUpdate);
    router.delete("/api/lobby/:id").produces("application/json").handler(this::handleDelete);
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
      VertxUtil.onRxError(routingContext)
    );
  }

  private void handleGetAllStream(final RoutingContext routingContext) {
    routingContext.response()
      .setChunked(true)
      .putHeader("Content-Type", "text/event-stream")
      .putHeader("Connection", "keep-alive")
      .putHeader("Cache-Control", "no-cache");

    this.streamService.getStreamAll().subscribe(
      event ->
        routingContext.response()
          .write(event.toString()),
      VertxUtil.onRxError(routingContext),
      () -> SSE.close(routingContext)
    );
  }

  private void handleGetById(final RoutingContext routingContext) {
    final Long id = Long.valueOf(routingContext.request().getParam("id"));

    this.service.get(id).subscribe(
        result ->
          routingContext.response()
            .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
            .setStatusCode(C.HTTP.ResponseCode.OK)
            .end(result.toJson().toString()),
        VertxUtil.onRxError(routingContext)
      );
  }

  private void handleGetByIdStream(final RoutingContext routingContext) {
    final Long id = Long.valueOf(routingContext.request().getParam("id"));

    routingContext.response()
      .setChunked(true)
      .putHeader("Content-Type", "text/event-stream")
      .putHeader("Connection", "keep-alive")
      .putHeader("Cache-Control", "no-cache");

    this.streamService.getStreamById(id).subscribe(
      result -> {
        Optional<SSE.Event<LobbyStreamEventType, Lobby>> eventOpt = Optional.ofNullable(result);
        routingContext.response()
          .write(eventOpt.map(SSE.Event::toString).orElse(""));
      },
      VertxUtil.onRxError(routingContext),
      () -> SSE.close(routingContext)
    );
  }

  private void handleCreate(final RoutingContext routingContext) {
    final Lobby lobby = new Lobby((JsonObject) Json.decodeValue(routingContext.getBodyAsString()));

    this.service.create(lobby).subscribe(
        result ->
          routingContext.response().setStatusCode(C.HTTP.ResponseCode.CREATED)
            .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
            .end(result.toJson().toString()),
        VertxUtil.onRxError(routingContext)
      );
  }

  private void handleUpdate(final RoutingContext routingContext) {
    final Long id = Long.valueOf(routingContext.request().getParam("id"));
    final Lobby newLobby = new Lobby((JsonObject) Json.decodeValue(routingContext.getBodyAsString()));

    this.service.update(id, newLobby).subscribe(
      result ->
        routingContext.response()
          .setStatusCode(C.HTTP.ResponseCode.OK)
          .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
          .end(result.toJson().toString()),
      VertxUtil.onRxError(routingContext)
    );
  }

  private void handleDelete(final RoutingContext routingContext) {
    final Long id = Long.valueOf(routingContext.request().getParam("id"));

    this.service.delete(id).subscribe(
      result ->
        routingContext.response()
          .setStatusCode(C.HTTP.ResponseCode.OK)
          .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
          .end(result.toJson().toString()),
      VertxUtil.onRxError(routingContext)
    );
  }
}
