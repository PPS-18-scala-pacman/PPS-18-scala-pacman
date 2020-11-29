package it.unibo.scalapacman.lobby.communication;

import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import it.unibo.scalapacman.lobby.model.Game;
import rx.Single;

public class GameActionsImpl implements GameActions {

  private final String serverUrl;
  private final WebClient client;

  public GameActionsImpl(final String serverUrl, final WebClient client) {
    this.serverUrl = serverUrl;
    this.client = client;
  }

  public Single<Game> startGame(Game game) {
    return this.client.post(this.serverUrl, "/games")
      .as(BodyCodec.json(Long.class))
      .rxSendJson(game)
      .map(HttpResponse::body)
      .map(id -> new Game(id, game.getComponents()));
  }
}
