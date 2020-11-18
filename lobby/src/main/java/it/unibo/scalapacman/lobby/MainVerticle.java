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
      .flatMap(this::initRepository)
      .flatMap(this::initService)
      .flatMap(service -> startHttpServer(config, service));
  }

  private Single<JsonObject> getConfiguration() {
    return ConfigRetriever.create(vertx).rxGetConfig();
  }

  private Single<Dao<Lobby>> initRepository(PgPool dbClient) {
    return Single.just(new LobbyDao(dbClient));
  }

  private Single<LobbyService> initService(Dao<Lobby> repository) {
    return Single.just(new LobbyService(repository));
  }

  private Single<HttpServer> startHttpServer(JsonObject config, LobbyService service) {
    return this.startHttpServer(config.getInteger("HTTP_PORT", 8080), service);
  }

  private Single<HttpServer> startHttpServer(Integer localPort, LobbyService service) {
    final Router router = Router.router(vertx);

    router.route().handler(corsHandler());
    router.route().handler(BodyHandler.create());

    new LobbyResource(router, service);

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
}
