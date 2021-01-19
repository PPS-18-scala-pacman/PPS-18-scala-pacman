package it.unibo.scalapacman.lobby.model;

import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Game implements Jsonable {

  private final String id;
  private final Map<String, Integer> components;
  private final String hostId;

  public Game(String id, Map<String, Integer> components, String hostId) {
    this.id = id;
    this.components = components;
    this.hostId = hostId;
  }

  public Game(Lobby lobby) {
    if (lobby == null || lobby.getParticipants() == null) throw new IllegalArgumentException("Lobby object is null or participants argument is null");
    if (lobby.getSize() > lobby.getParticipants().size()) throw new IllegalStateException("Lobby is not full, can't create a new game");

    this.id = lobby.getGameId();
    this.components = lobby.getParticipants().stream()
      .collect(Collectors.toMap(Participant::getUsername, Participant::getPacmanTypeAsInteger));

    this.hostId = null;
  }

  public String getId() {
    return id;
  }

  public Map<String, Integer> getComponents() {
    return components;
  }

  public String getHostId() {
    return hostId;
  }

  @Override
  public JsonObject toJson() {
    Map<String, Object> components = new HashMap<>(this.components);

    return new JsonObject()
      .put("components", new JsonObject(components))
      .put("hostId", hostId);
  }
}
