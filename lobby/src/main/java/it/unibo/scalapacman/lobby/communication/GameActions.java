package it.unibo.scalapacman.lobby.communication;

import io.reactivex.Single;
import it.unibo.scalapacman.lobby.model.Game;

public interface GameActions {
  Single<Game> startGame(Game game);
}
