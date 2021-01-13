package it.unibo.scalapacman.lobby.util;

import io.reactivex.functions.Consumer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.ext.web.RoutingContext;
import it.unibo.scalapacman.lobby.util.exception.APIException;

public class VertxUtil {
  private static final Logger logger = LoggerFactory.getLogger(VertxUtil.class);

  public static Consumer<Throwable> onRoutingError(final RoutingContext routingContext) {
    return routingContext::fail;
  }

  public static void onError(final RoutingContext routingContext) {
    handleException(routingContext.failure());
  }

  public static void handleException(final Throwable ex) {
    if (ex instanceof APIException) {
      logger.info(ex.getMessage());
    } else {
      logger.error(ex.getMessage(), ex);
    }
  }
}
