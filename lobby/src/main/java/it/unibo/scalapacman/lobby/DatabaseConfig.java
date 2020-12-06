package it.unibo.scalapacman.lobby;

import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;

public class DatabaseConfig {
  private static final int RECONNECT_ATTEMPTS = 10;
  private static final int RECONNECT_INTERVAL_MS = 10000;

  static PgConnectOptions create(JsonObject config) {
    return new PgConnectOptions()
      .setPort(config.getInteger("PORT", 5432))
      .setHost(config.getString("HOST", "localhost"))
      .setDatabase(config.getString("DATABASE", "lobby"))
      .setUser(config.getString("USER", "postgres"))
      .setPassword(config.getString("PASSWORD", "postgres"))
      .setReconnectAttempts(RECONNECT_ATTEMPTS)
      .setReconnectInterval(RECONNECT_INTERVAL_MS);
  }
}
