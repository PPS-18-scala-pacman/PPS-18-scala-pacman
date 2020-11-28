package it.unibo.scalapacman.lobby.util;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.ext.web.RoutingContext;
import it.unibo.scalapacman.lobby.util.exception.APIException;
import rx.functions.Action1;

public class VertxUtil {
  private static final Logger logger = LoggerFactory.getLogger(VertxUtil.class);

  public static Action1<Throwable> onRoutingError(final RoutingContext routingContext) {
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
