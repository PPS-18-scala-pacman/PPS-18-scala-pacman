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
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.CorsHandler;
import io.vertx.rxjava.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import it.unibo.scalapacman.lobby.communication.GameActions;
import it.unibo.scalapacman.lobby.communication.GameActionsImpl;
import it.unibo.scalapacman.lobby.dao.LobbyDao;
import it.unibo.scalapacman.lobby.dao.LobbyDaoImpl;
import it.unibo.scalapacman.lobby.dao.ParticipantDao;
import it.unibo.scalapacman.lobby.dao.ParticipantDaoImpl;
import it.unibo.scalapacman.lobby.resource.LobbyResource;
import it.unibo.scalapacman.lobby.resource.ParticipantResource;
import it.unibo.scalapacman.lobby.service.GameService;
import it.unibo.scalapacman.lobby.service.LobbyService;
import it.unibo.scalapacman.lobby.service.LobbyStreamService;
import it.unibo.scalapacman.lobby.service.ParticipantService;
import it.unibo.scalapacman.lobby.util.VertxUtil;
import it.unibo.scalapacman.lobby.util.exception.APIException;
import rx.Completable;
import rx.Single;

import java.util.HashSet;
import java.util.Set;

public class MainVerticle extends AbstractVerticle {

  private final Logger logger = this.initMainLogger();

  @Override
  public Completable rxStart() {
    vertx.exceptionHandler(VertxUtil::handleException);
    return getConfiguration()
      .flatMap(this::start)
      .toCompletable();
  }

  public Single<?> start(JsonObject config) {
    return prepareDatabase(config.getJsonObject("DATABASE"))
      .flatMap(dbClient -> this.initExternalInterfaces(config, dbClient, WebClient.create(vertx)))
      .flatMap(this::initServices)
      .flatMap(services -> startHttpServer(config, services));
  }

  private Single<JsonObject> getConfiguration() {
    return ConfigRetriever.create(vertx).rxGetConfig();
  }

  private Single<ExternalInterfacesContainer> initExternalInterfaces(JsonObject config, PgPool dbClient, WebClient webClient) {
    return Single.just(new ExternalInterfacesContainer(
      new LobbyDaoImpl(dbClient),
      new ParticipantDaoImpl(dbClient),
      new GameActionsImpl(config.getInteger("GAME_SERVER_PORT"), config.getString("GAME_SERVER_URL"), webClient)
    ));
  }

  private Single<ServiceContainer> initServices(ExternalInterfacesContainer container) {
    LobbyStreamService lobbyStreamService = new LobbyStreamService(container.lobby);
    LobbyService lobbyService = new LobbyService(container.lobby, lobbyStreamService);
    return Single.just(new ServiceContainer(
      lobbyStreamService,
      lobbyService,
      new ParticipantService(container.participant, lobbyStreamService),
      new GameService(container.game, lobbyStreamService, lobbyService)
    ));
  }

  private Single<HttpServer> startHttpServer(JsonObject config, ServiceContainer services) {
    return this.startHttpServer(config.getInteger("HTTP_PORT", 8080), services);
  }

  private Single<HttpServer> startHttpServer(Integer localPort, ServiceContainer services) {
    final Router router = Router.router(vertx);

    router.route().handler(corsHandler());
    router.route().handler(BodyHandler.create());

    new LobbyResource(router, services.lobby, services.lobbyStream, services.game);
    new ParticipantResource(router, services.participant);

    router.route().failureHandler(ctx -> {
      VertxUtil.onError(ctx);

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
      .rxListen(localPort, "0.0.0.0")
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

  private static class ExternalInterfacesContainer {
    final LobbyDao lobby;
    final ParticipantDao participant;
    final GameActions game;

    ExternalInterfacesContainer(final LobbyDao lobby, final ParticipantDao participant, final GameActions game) {
      this.lobby = lobby;
      this.participant = participant;
      this.game = game;
    }
  }

  private static class ServiceContainer {
    final LobbyStreamService lobbyStream;
    final LobbyService lobby;
    final ParticipantService participant;
    final GameService game;

    ServiceContainer(final LobbyStreamService lobbyStream, final LobbyService lobby, final ParticipantService participant, final GameService game) {
      this.lobbyStream = lobbyStream;
      this.lobby = lobby;
      this.participant = participant;
      this.game = game;
    }
  }
}
