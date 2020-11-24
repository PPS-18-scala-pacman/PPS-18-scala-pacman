package it.unibo.scalapacman.lobby.model;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Lobby {

  private final Long id;
  private String description;
  private Integer size;
  private List<Participant> participants;

  public Lobby(final Long id, final String description, Integer size) {
    this(id, description, size, new ArrayList<>(size));
  }

  public Lobby(final Long id, final String description, Integer size, List<Participant> participants) {
    if (participants.size() > size) throw new IllegalArgumentException("participants can't have more elements than size value");
    this.id = id;
    this.description = description;
    this.size = size;
    this.participants = participants;
  }

  public Lobby(final JsonObject json) {
    this(
      json.getLong("id"),
      json.getString("description"),
      json.getInteger("size"),
      Optional.ofNullable(json.getJsonArray("participants")).orElse(new JsonArray()).stream()
        .map(jsonObj -> new Participant((JsonObject) jsonObj))
        .collect(Collectors.toList())
    );
  }

  public Long getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public List<Participant> getAttendees() {
    return participants;
  }

  public void setAttendees(List<Participant> participants) {
    this.participants = participants;
  }

  public String toString() {
    return Json.encodePrettily(this);
  }

  public JsonObject toJson() {
    return new JsonObject()
      .put("id", this.id)
      .put("description", this.description)
      .put("size", this.size)
      .put("participants", this.participants.stream().map(Participant::toJson).collect(Collectors.toList()));
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
        && this.description.equals(other.getDescription());
    }

    return false;
  }
}
