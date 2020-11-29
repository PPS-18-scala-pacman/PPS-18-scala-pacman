package it.unibo.scalapacman.lobby.communication;

import it.unibo.scalapacman.lobby.model.Game;
import rx.Single;

public interface GameActions {
  Single<Game> startGame(Game game);
}
