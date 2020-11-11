package it.unibo.scalapacman.lobby;

import java.util.HashSet;
import java.util.Set;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public class MainVerticle extends AbstractVerticle {

  private final Logger logger = this.initMainLogger();

  @Override
  public void start(Promise promise) {
    Future<Void> steps = getConfiguration()
      .compose(this::start);

    steps.onComplete((AsyncResult<Void> result) -> {
      if (result.failed()) {
        promise.fail(result.cause());
      } else {
        promise.complete();
      }
    });
  }

  public Future<Void> start(JsonObject config) {
    return prepareDatabase(config.getJsonObject("DATABASE"))
      .compose(this::createRepository)
      .compose(this::createService)
      .compose(service -> startHttpServer(config, service));
  }

  private Future<JsonObject> getConfiguration() {
    Promise<JsonObject> promise = Promise.promise();
    ConfigRetriever.create(vertx)
      .getConfig(config -> {
        if (config.failed()) {
          promise.fail(config.cause());
        } else {
          JsonObject result = config.result();
          promise.complete(result);
        }
      });
    return promise.future();
  }

  private Future<LobbyRepository> createRepository(PgPool dbClient) {
    Promise<LobbyRepository> promise = Promise.promise();
    promise.complete(new LobbyRepository(dbClient));
    return promise.future();
  }

  private Future<LobbyService> createService(LobbyRepository repository) {
    Promise<LobbyService> promise = Promise.promise();
    promise.complete(new LobbyService(repository));
    return promise.future();
  }

  private Future<Void> startHttpServer(JsonObject config, LobbyService service) {
    return this.startHttpServer(config.getInteger("HTTP_PORT", 8080), service);
  }

  private Future<Void> startHttpServer(Integer localPort, LobbyService service) {
    Promise<Void> promise = Promise.promise();
    final Router router = Router.router(vertx);

    router.route().handler(corsHandler());
    router.route().handler(BodyHandler.create());

    new LobbyResource(router, service);

    //router.route().handler(StaticHandler.create().setWebRoot("webroot/myname").setCachingEnabled(false));

    getVertx().createHttpServer().requestHandler(router).listen(localPort, ar -> {
      if (ar.succeeded()) {
        logger.info("HTTP server running on port " + localPort);
        promise.complete();
      } else {
        logger.error("Could not start a HTTP server", ar.cause());
        promise.fail(ar.cause());
      }
    });
    return promise.future();
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

  private Future<PgPool> prepareDatabase(JsonObject config) {
    return this.prepareDatabase(DatabaseConfig.create(config));
  }

  private Future<PgPool> prepareDatabase(PgConnectOptions connectOptions) {
    Promise<PgPool> promise = Promise.promise();

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    PgPool pgPool = PgPool.pool(vertx, connectOptions, poolOptions);

    promise.complete(pgPool);
    return promise.future();
  }

  private Logger initMainLogger() {
    // Setting up SLF4J
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    LoggerFactory.initialise();
    return LoggerFactory.getLogger(MainVerticle.class);
  }
}
