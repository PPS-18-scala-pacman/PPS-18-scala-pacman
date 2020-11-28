package it.unibo.scalapacman.lobby.service;

import it.unibo.scalapacman.lobby.util.REST;

public class LobbyStreamEventType {
  LobbyStreamObject object;
  REST httpType;

  public LobbyStreamEventType(LobbyStreamObject object, REST httpType) {
    this.object = object;
    this.httpType = httpType;
  }

  public LobbyStreamObject getObject() {
    return object;
  }

  public REST getHttpType() {
    return httpType;
  }

  @Override
  public String toString() {
    return String.format("%s/%s", this.object, this.httpType);
  }
}
