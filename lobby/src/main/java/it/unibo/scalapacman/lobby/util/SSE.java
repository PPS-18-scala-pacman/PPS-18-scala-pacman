package it.unibo.scalapacman.lobby.util;

import io.vertx.core.VertxException;
import io.vertx.rxjava.ext.web.RoutingContext;

public class SSE {
  static public void close(final RoutingContext routingContext) {
    try {
      routingContext.response().end(); // best effort
    } catch(VertxException | IllegalStateException e) {
      // connection has already been closed by the browser
      // do not log to avoid performance issues (ddos issue if client opening and closing alot of connections abruptly)
    }
  }
}
