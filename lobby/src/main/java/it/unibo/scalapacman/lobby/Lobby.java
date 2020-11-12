package it.unibo.scalapacman.lobby;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class Lobby {

  private final Integer id;
  private String description;

  public Lobby(final Integer id, final String description) {
    this.id = id;
    this.description = description;
  }

  public Lobby(JsonObject json) {
    this.id = json.getInteger("id");
    this.description = json.getString("description");
  }

  public Integer getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String toString() {
    return Json.encodePrettily(this);
  }

  public JsonObject toJson() {
    return new JsonObject()
      .put("id", this.id)
      .put("description", this.description);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj instanceof Lobby) {
      final Lobby other = (Lobby) obj;

      return this.id.equals(other.id)
        && this.description.equals(other.description);
    }

    return false;
  }
}

