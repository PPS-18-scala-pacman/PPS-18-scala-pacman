package it.unibo.scalapacman.lobby.util;

import io.vertx.core.json.JsonArray;
import it.unibo.scalapacman.lobby.model.Jsonable;

import java.util.ArrayList;
import java.util.Collection;

public class ListJsonable<E extends Jsonable> extends ArrayList<E> implements Jsonable {

  public ListJsonable() {
    super();
  }

  public ListJsonable(Collection<? extends E> c) {
    super(c);
  }

  @Override
  public JsonArray toJson() {
    return this.stream()
      .map(Jsonable::toJson)
      .collect(JsonCollector.toJsonArray());
  }
}
