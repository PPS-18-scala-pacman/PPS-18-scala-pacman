package it.unibo.scalapacman.lobby.communication;

import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import it.unibo.scalapacman.lobby.model.Game;
import rx.Single;

public class GameActionsImpl implements GameActions {

  private final int serverPort;
  private final String serverUrl;
  private final WebClient client;

  public GameActionsImpl(final int serverPort, final String serverUrl, final WebClient client) {
    this.serverPort = serverPort;
    this.serverUrl = serverUrl;
    this.client = client;
  }

  public Single<Game> startGame(Game game) {
    return this.client.post(this.serverPort, this.serverUrl, "/api/games")
      .as(BodyCodec.string())
      .rxSendJson(game.toJson())
      .map(HttpResponse::body)
      .map(id -> new Game(id, game.getComponents()));
  }
}
