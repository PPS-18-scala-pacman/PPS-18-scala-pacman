package it.unibo.scalapacman.lobby.util;

import io.vertx.core.VertxException;
import io.vertx.rxjava.ext.web.RoutingContext;
import it.unibo.scalapacman.lobby.model.Jsonable;

public class SSE {
  static public void close(final RoutingContext routingContext) {
    try {
      routingContext.response().end(); // best effort
    } catch(VertxException | IllegalStateException e) {
      // connection has already been closed by the browser
      // do not log to avoid performance issues (ddos issue if client opening and closing alot of connections abruptly)
    }
  }

  public static class Event<T, D extends Jsonable> {
    private final static String template = "event: %s\ndata: %s\n\n";

    private final T type;
    private final D data;

    public Event(T type, D data) {
      this.type = type;
      this.data = data;
    }

    public T getType() {
      return type;
    }

    public D getData() {
      return data;
    }

    @Override
    public String toString() {
      return String.format(template, type.toString(), data == null ? "" : data.toJson().toString());
    }
  }
}
