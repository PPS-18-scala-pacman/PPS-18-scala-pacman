package it.unibo.scalapacman.lib.model

import it.unibo.scalapacman.lib.model.GameTimedEventType.GameTimedEventType

case class GameTimedEvent[+T](
                              eventType: GameTimedEventType,
                              timeMs: Option[Double] = None,
                              dots: Option[Int] = None,
                              payload: Option[T] = None
                            )
