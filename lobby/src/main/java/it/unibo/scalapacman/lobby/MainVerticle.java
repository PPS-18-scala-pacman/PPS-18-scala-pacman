package it.unibo.scalapacman.lobby;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.rxjava.config.ConfigRetriever;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.CorsHandler;
import io.vertx.rxjava.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import it.unibo.scalapacman.lobby.dao.Dao;
import it.unibo.scalapacman.lobby.dao.LobbyDao;
import it.unibo.scalapacman.lobby.dao.ParticipantDao;
import it.unibo.scalapacman.lobby.model.Lobby;
import it.unibo.scalapacman.lobby.model.Participant;
import it.unibo.scalapacman.lobby.resource.LobbyResource;
import it.unibo.scalapacman.lobby.resource.ParticipantResource;
import it.unibo.scalapacman.lobby.service.LobbyService;
import it.unibo.scalapacman.lobby.service.LobbyStreamService;
import it.unibo.scalapacman.lobby.service.ParticipantService;
import it.unibo.scalapacman.lobby.util.exception.APIException;
import rx.Completable;
import rx.Single;

import java.util.HashSet;
import java.util.Set;

public class MainVerticle extends AbstractVerticle {

  private final Logger logger = this.initMainLogger();

  @Override
  public Completable rxStart() {
    return getConfiguration()
      .flatMap(this::start)
      .toCompletable();
  }

  public Single<?> start(JsonObject config) {
    return prepareDatabase(config.getJsonObject("DATABASE"))
      .flatMap(this::initDao)
      .flatMap(this::initServices)
      .flatMap(services -> startHttpServer(config, services));
  }

  private Single<JsonObject> getConfiguration() {
    return ConfigRetriever.create(vertx).rxGetConfig();
  }

  private Single<DaoContainer> initDao(PgPool dbClient) {
    return Single.just(new DaoContainer(
      new LobbyDao(dbClient),
      new ParticipantDao(dbClient)
    ));
  }

  private Single<ServiceContainer> initServices(DaoContainer dao) {
    LobbyStreamService lobbyStreamService = new LobbyStreamService(dao.lobby);
    return Single.just(new ServiceContainer(
      lobbyStreamService,
      new LobbyService(dao.lobby, lobbyStreamService),
      new ParticipantService(dao.participant, lobbyStreamService)
    ));
  }

  private Single<HttpServer> startHttpServer(JsonObject config, ServiceContainer services) {
    return this.startHttpServer(config.getInteger("HTTP_PORT", 8080), services);
  }

  private Single<HttpServer> startHttpServer(Integer localPort, ServiceContainer services) {
    final Router router = Router.router(vertx);

    router.route().handler(corsHandler());
    router.route().handler(BodyHandler.create());

    new LobbyResource(router, services.lobby, services.lobbyStream);
    new ParticipantResource(router, services.participant);

    //router.route().handler(StaticHandler.create().setWebRoot("webroot/myname").setCachingEnabled(false));

    router.route().failureHandler(ctx -> {
      final JsonObject error = new JsonObject()
        .put("timestamp", System.nanoTime())
        .put("exception", ctx.failure().getClass().getName())
        .put("exceptionMessage", ctx.failure().getMessage())
        .put("path", ctx.request().path());

      ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
        .setStatusCode(ctx.failure() instanceof APIException ? ((APIException) ctx.failure()).getCode() : C.HTTP.ResponseCode.INTERNAL_SERVER_ERROR)
        .end(error.encode());
    });

    return vertx.createHttpServer()
      .requestHandler(router)
      .rxListen(localPort)
      .doOnSuccess(res -> logger.info("HTTP server running on port " + localPort));
  }

  private CorsHandler corsHandler() {
    final Set<String> allowHeaders = new HashSet<>();
    allowHeaders.add("x-requested-with");
    allowHeaders.add("Access-Control-Allow-Origin");
    allowHeaders.add("origin");
    allowHeaders.add("Content-Type");
    allowHeaders.add("accept");

    final Set<HttpMethod> allowMethods = new HashSet<>();
    allowMethods.add(HttpMethod.GET);
    allowMethods.add(HttpMethod.POST);
    allowMethods.add(HttpMethod.DELETE);
    allowMethods.add(HttpMethod.PUT);

    return CorsHandler.create("*").allowedHeaders(allowHeaders).allowedMethods(allowMethods);
  }

  private Single<PgPool> prepareDatabase(JsonObject config) {
    return this.prepareDatabase(DatabaseConfig.create(config));
  }

  private Single<PgPool> prepareDatabase(PgConnectOptions connectOptions) {
    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    PgPool pgPool = PgPool.pool(vertx, connectOptions, poolOptions);

    return Single.just(pgPool);
  }

  private Logger initMainLogger() {
    // Setting up SLF4J
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    LoggerFactory.initialise();
    return LoggerFactory.getLogger(MainVerticle.class);
  }

  private static class DaoContainer {
    final Dao<Lobby, Long> lobby;
    final Dao<Participant, String> participant;

    DaoContainer(Dao<Lobby, Long> lobby, Dao<Participant, String> participant) {
      this.lobby = lobby;
      this.participant = participant;
    }
  }

  private static class ServiceContainer {
    final LobbyStreamService lobbyStream;
    final LobbyService lobby;
    final ParticipantService participant;

    ServiceContainer(final LobbyStreamService lobbyStream, final LobbyService lobby, final ParticipantService participant) {
      this.lobbyStream = lobbyStream;
      this.lobby = lobby;
      this.participant = participant;
    }
  }
}
