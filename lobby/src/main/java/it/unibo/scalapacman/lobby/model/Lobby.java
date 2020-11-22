package it.unibo.scalapacman.lobby.model;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Lobby {

  private final Integer id;
  private String description;
  private Integer size;
  private List<Attendee> attendees;

  public Lobby(final Integer id, final String description, Integer size) {
    this(id, description, size, new ArrayList<>(size));
  }

  public Lobby(final Integer id, final String description, Integer size, List<Attendee> attendees) {
    if (attendees.size() > size) throw new IllegalArgumentException("attendees can't have more elements than size value");
    this.id = id;
    this.description = description;
    this.size = size;
    this.attendees = attendees;
  }

  public Lobby(JsonObject json) {
    this.id = json.getInteger("id");
    this.description = json.getString("description");
    this.size = json.getInteger("size");
    this.attendees = json.getJsonArray("attendees").stream()
      .map(jsonObj -> new Attendee((JsonObject) jsonObj))
      .collect(Collectors.toList());
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

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public List<Attendee> getAttendees() {
    return attendees;
  }

  public void setAttendees(List<Attendee> attendees) {
    this.attendees = attendees;
  }

  public String toString() {
    return Json.encodePrettily(this);
  }

  public JsonObject toJson() {
    return new JsonObject()
      .put("id", this.id)
      .put("description", this.description)
      .put("size", this.size)
      .put("attendees", this.attendees);
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
