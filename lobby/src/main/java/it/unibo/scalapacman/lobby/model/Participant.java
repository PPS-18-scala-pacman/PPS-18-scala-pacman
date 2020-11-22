package it.unibo.scalapacman.lobby.model;

import io.vertx.core.json.JsonObject;
import it.unibo.scalapacman.lib.model.PacmanType;

public class Participant {
  private String username;
  private Boolean host;
  private PacmanType.PacmanType pacmanType;
  private Long lobbyId;

  public Participant(final String username, final Boolean host, final Integer pacmanType, final Long lobbyId) {
    this(username, host, PacmanType.indexToPlayerTypeVal(pacmanType), lobbyId);
  }

  public Participant(final String username, final Boolean host, final PacmanType.PacmanType pacmanType, final Long lobbyId) {
    this.username = username;
    this.host = host;
    this.pacmanType = pacmanType;
    this.lobbyId = lobbyId;
  }

  public Participant(JsonObject json) {
    this (
      json.getString("username"),
      json.getBoolean("host"),
      json.getInteger("pacmanType"),
      json.getLong("lobbyId")
    );
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Boolean getHost() {
    return host;
  }

  public void setHost(Boolean host) {
    this.host = host;
  }

  public PacmanType.PacmanType getPacmanType() {
    return pacmanType;
  }

  public void setPacmanType(PacmanType.PacmanType pacmanType) {
    this.pacmanType = pacmanType;
  }

  public Long getLobbyId() {
    return lobbyId;
  }

  public void setLobbyId(Long lobbyId) {
    this.lobbyId = lobbyId;
  }

  public JsonObject toJson() {
    return new JsonObject()
      .put("username", this.username)
      .put("host", this.host)
      .put("pacmanType", PacmanType.playerTypeValToIndex(this.pacmanType))
      .put("lobbyId", this.lobbyId);
  }

  @Override
  public int hashCode() {
    final int prime = 17;
    int result = 1;
    result = prime * result + ((host == null) ? 0 : host.hashCode());
    result = prime * result + ((username == null) ? 0 : username.hashCode());
    result = prime * result + ((pacmanType == null) ? 0 : pacmanType.hashCode());
    result = prime * result + ((lobbyId == null) ? 0 : lobbyId.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj instanceof Participant) {
      final Participant other = (Participant) obj;

      return this.username.equals(other.username)
        && this.host.equals(other.host)
        && this.pacmanType.equals(other.pacmanType)
        && this.lobbyId.equals(other.lobbyId);
    }

    return false;
  }
}
