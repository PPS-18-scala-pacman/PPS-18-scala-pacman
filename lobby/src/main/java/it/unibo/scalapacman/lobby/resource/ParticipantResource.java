package it.unibo.scalapacman.lobby.resource;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import it.unibo.scalapacman.lobby.C;
import it.unibo.scalapacman.lobby.model.Participant;
import it.unibo.scalapacman.lobby.service.ParticipantService;
import it.unibo.scalapacman.lobby.util.JsonCollector;
import it.unibo.scalapacman.lobby.util.ResourceUtil;

public class ParticipantResource {
  private static final Logger logger = LoggerFactory.getLogger(ParticipantResource.class);

  private final ParticipantService service;

  public ParticipantResource(Router router, ParticipantService service) {
    this.service = service;

    router.get("/api/participant").produces("application/json").handler(this::handleGetAll);
    router.get("/api/participant/:username").produces("application/json").handler(this::handleGetById);
    router.post("/api/participant").produces("application/json").handler(this::handleCreate);
    router.put("/api/participant/:username").produces("application/json").handler(this::handleUpdate);
    router.delete("/api/participant/:username").produces("application/json").handler(this::handleDelete);
  }

  private void handleGetAll(final RoutingContext routingContext) {
    this.service.getAll().subscribe(
      participants -> {
        final JsonArray array = participants.stream()
          .map(Participant::toJson)
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
    final String id = routingContext.request().getParam("id");

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
    final Participant participant = new Participant((JsonObject) Json.decodeValue(routingContext.getBodyAsString()));

    this.service.create(participant).subscribe(
        result ->
          routingContext.response().setStatusCode(C.HTTP.ResponseCode.CREATED)
            .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
            .end(result.toJson().toString()),
        ResourceUtil.onError(routingContext)
      );
  }

  private void handleUpdate(final RoutingContext routingContext) {
    final String id = routingContext.request().getParam("username");
    final Participant newParticipant = new Participant((JsonObject) Json.decodeValue(routingContext.getBodyAsString()));

    this.service.update(id, newParticipant).subscribe(
      result ->
        routingContext.response()
          .setStatusCode(C.HTTP.ResponseCode.OK)
          .putHeader(C.HTTP.HeaderElement.CONTENT_TYPE, C.HTTP.HeaderElement.ContentType.APPLICATION_JSON)
          .end(result.toJson().toString()),
      ResourceUtil.onError(routingContext)
    );
  }

  private void handleDelete(final RoutingContext routingContext) {
    final String id = routingContext.request().getParam("username");

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
