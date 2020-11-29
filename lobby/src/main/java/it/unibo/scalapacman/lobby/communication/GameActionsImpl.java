package it.unibo.scalapacman.lobby.communication;

import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import it.unibo.scalapacman.lobby.model.Game;
import rx.Single;

public class GameActionsImpl implements GameActions {

  private final WebClient client;

  public GameActionsImpl(WebClient client) {
    this.client = client;
  }

  public Single<Game> startGame(Game game) {
    // http://$serverURL
    return this.client.post("http://localhost:8080", "/games")
      .as(BodyCodec.json(Long.class))
      .rxSendJson(game)
      .map(HttpResponse::body)
      .map(id -> new Game(id, game.getComponents()));
  }
}
