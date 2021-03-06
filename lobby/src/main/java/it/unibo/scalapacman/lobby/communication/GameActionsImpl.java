package it.unibo.scalapacman.lobby.communication;

import io.reactivex.Single;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import it.unibo.scalapacman.lobby.model.Game;

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
      .map(response -> new Game(response.body(), game.getComponents(), response.getHeader("X-Real-IP")));
  }
}
