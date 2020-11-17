package it.unibo.scalapacman.lobby;

import io.vertx.core.json.JsonObject;

public class AttendeeTemp {
  private String username;
  private Boolean host;

  public AttendeeTemp(final String username, final Boolean host) {
    this.username = username;
    this.host = host;
  }

  public AttendeeTemp(JsonObject json) {
    this.username = json.getString("username");
    this.host = json.getBoolean("host");
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

  public JsonObject toJson() {
    return new JsonObject()
      .put("username", this.username)
      .put("host", this.host);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((host == null) ? 0 : host.hashCode());
    result = prime * result + ((username == null) ? 0 : username.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj instanceof AttendeeTemp) {
      final AttendeeTemp other = (AttendeeTemp) obj;

      return this.username.equals(other.username)
        && this.host.equals(other.host);
    }

    return false;
  }
}
