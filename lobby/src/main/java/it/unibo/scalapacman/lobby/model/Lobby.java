package it.unibo.scalapacman.lobby.model;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Lobby implements Jsonable {

  private final Long id;
  private String description;
  private Short size;
  private String hostUsername;
  private List<Participant> participants;
  private Long gameId;

  public Lobby(final Long id, final String description, Short size, String hostUsername) {
    this(id, description, size, hostUsername, new ArrayList<>(size));
  }

  public Lobby(final Long id, final String description, Short size, String hostUsername, List<Participant> participants) {
    this(id, description, size, hostUsername, participants, null);
  }

  public Lobby(final Long id, final String description, Short size, String hostUsername, List<Participant> participants, Long gameId) {
    if (participants.size() > size) throw new IllegalArgumentException("participants can't have more items than size value");
    this.id = id;
    this.description = description;
    this.size = size;
    this.hostUsername = hostUsername;
    this.participants = participants;
    this.gameId = gameId;
  }

  public Lobby(final JsonObject json) {
    this(
      json.getLong("id"),
      json.getString("description"),
      json.getInteger("size").shortValue(),
      json.getString("hostUsername"),
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

  public Short getSize() {
    return size;
  }

  public void setSize(Short size) {
    this.size = size;
  }

  public String getHostUsername() {
    return hostUsername;
  }

  public void setHostUsername(String hostUsername) {
    this.hostUsername = hostUsername;
  }

  public List<Participant> getParticipants() {
    return participants;
  }

  public void setParticipants(List<Participant> participants) {
    this.participants = participants;
  }

  public Long getGameId() {
    return gameId;
  }

  public void setGameId(Long gameId) {
    this.gameId = gameId;
  }

  public String toString() {
    return Json.encodePrettily(this);
  }

  public JsonObject toJson() {
    return new JsonObject()
      .put("id", this.id)
      .put("description", this.description)
      .put("size", this.size)
      .put("hostUsername", this.hostUsername)
      .put("participants", this.participants.stream().map(Participant::toJson).collect(Collectors.toList()))
      .put("gameId", this.gameId);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((size == null) ? 0 : size.hashCode());
    result = prime * result + ((hostUsername == null) ? 0 : hostUsername.hashCode());
    result = prime * result + ((gameId == null) ? 0 : gameId.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj instanceof Lobby) {
      final Lobby other = (Lobby) obj;

      return this.id.equals(other.id)
        && this.description.equals(other.getDescription())
        && this.size.equals(other.getSize())
        && this.hostUsername.equals(other.getHostUsername())
        && this.gameId.equals(other.getGameId());
    }

    return false;
  }
}
