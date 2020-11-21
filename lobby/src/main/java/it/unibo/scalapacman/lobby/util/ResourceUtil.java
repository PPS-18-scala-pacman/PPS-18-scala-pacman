package it.unibo.scalapacman.lobby.util;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.ext.web.RoutingContext;
import it.unibo.scalapacman.lobby.util.exception.APIException;
import rx.functions.Action1;

public class ResourceUtil {
  private static final Logger logger = LoggerFactory.getLogger(ResourceUtil.class);

  public static Action1<Throwable> onError(final RoutingContext routingContext) {
    return (Throwable ex) -> {
      if (ex instanceof APIException) {
        logger.info(ex.getMessage());
      } else {
        logger.error(ex.getMessage(), ex);
      }
      routingContext.fail(ex);
    };
  }
}
