package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.model.{Character, Dot, Eatable, Fruit, GameObject, GameState, GameTimedEvent,
  Level, LevelState, Map, SpeedCondition, Tile}
import it.unibo.scalapacman.lib.model.GameTimedEventType.{FRUIT_STOP, GHOST_RESTART, FRUIT_SPAWN, ENERGIZER_STOP}
import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper, MapHelper}
import it.unibo.scalapacman.lib.model.Character.{Ghost, Pacman}
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.lib.model.Level.{ghostSpeed, pacmanSpeed}
import it.unibo.scalapacman.lib.model.LevelState.LevelState
import it.unibo.scalapacman.lib.model.SpeedCondition.SpeedCondition

import scala.concurrent.duration.FiniteDuration

object GameTick {
  /**
   * Ritorna le collisioni tra Pacman e Fantasmi e tra Pacman e oggetti Eatable.
   * Una collisione avviene quando due personaggi si trovano all'interno della stessa tile e quando
   * un personaggio si trova in una tile che contiene un oggetto.
   * @param characters I personaggi in gioco
   * @param map La mappa di gioco
   * @return Le collisioni rilevate
   */
  def collisions(characters: List[Character])(implicit map: Map): List[(Character, GameObject)] = {
    def characterGhostCollisions(character: Character): List[(Character, GameObject)] =
      characters filter(!_.isInstanceOf[Pacman]) filter(_.tileIndexes == character.tileIndexes) map(obj => (character, obj))
    def characterEatableCollisions(character: Character): List[(Character, GameObject)] =
      character.tile.eatable map(obj => (character, obj)) toList

    val pacmans = characters collect { case p: Pacman => p }

    (pacmans flatMap characterGhostCollisions).:::((pacmans flatMap characterEatableCollisions).groupBy(_._2).map(_._2.head)(collection.breakOut))
  }

  /**
   * Aggiorna lo stato del gioco in base alle collisioni.
   * Quando necessario aggiorna il punteggio e attiva ghostInFear e pacmanEmpowered.
   * @param gameState Stato corrente
   * @param collisions Collisioni
   * @return Lo stato di gioco aggiornato
   */
  def calculateGameState(gameState: GameState)(implicit collisions: List[(Character, GameObject)]): GameState = gameState.copy(
    score = (collisions collect { case (_, e: Eatable) => e } map (_.points) sum) + gameState.score,
    ghostInFear = gameState.ghostInFear || isEnergizerEaten,
    pacmanEmpowered = gameState.pacmanEmpowered || isEnergizerEaten,
  )

  /**
   * Verifica se è stato mangiato un energizer
   * @param collisions Collisioni
   * @return true se è stato mangiato un energizer, false altrimenti
   */
  private def isEnergizerEaten(implicit collisions: List[(Character, GameObject)]) =
    collisions.exists {
      case (_, Dot.ENERGIZER_DOT) => true
      case _ => false
    }

  /**
   * Aggiorna la mappa rimuovendo eventuali oggetti mangiati
   * @param map Mappa corrente
   * @param collisions Collisioni
   * @return La mappa aggiornata
   */
  def calculateMap(map: Map)(implicit collisions: List[(Character, GameObject)]): Map = map.copy(
    tiles = calculateMapTiles(map, collisions.collect({ case (a: Character, e: Eatable) => (a, e) }))
  )

  private def calculateMapTiles(map: Map, collisions: List[(Character, Eatable)]): List[List[Tile]] =
    collisions.map(_._1).foldLeft(map)(calculateMapTile(_, _)).tiles

  private def calculateMapTile(implicit map: Map, character: Character): Map = character.eat

  /**
   * Aggiorna lo stato isDead dei personaggi in input. Viene modificato quando si verifica una collisione
   * tra due Character e il risultato varia in base allo stato corrente di gioco.
   * @param characters Personaggi in gioco
   * @param gameState Stato di gioco corrente
   * @param collisions Collisioni
   * @param map Mappa di gioco
   * @return I personaggi aggiornati
   */
  def calculateDeaths(characters: List[Character], gameState: GameState)(implicit collisions: List[(Character, GameObject)], map: Map): List[Character] =
    for (
      character <- characters
    ) yield calculateDeath(character, gameState, collisions.collect({ case (c1: Character, c2: Character) => (c1, c2) }))

  private def calculateDeath(character: Character, gameState: GameState, collisions: List[(Character, Character)])(implicit map: Map): Character = {
    def pacmanCollideGhost(pacman: Pacman)(collision: (Character, Character)): Boolean = collision match {
      case (p: Pacman, _: Ghost) if p.characterType == pacman.characterType => true
      case _ => false
    }
    def killGhost(ghost: Ghost): Ghost = ghost.copy(isDead = true, position = Map.getRestartPosition(map.mapType, Ghost, ghost.characterType))
    def killPacman(pacman: Pacman): Pacman = pacman.copy(isDead = true, position = Map.getRestartPosition(map.mapType, Pacman, pacman.characterType))

    if (ghostCanBeKilled(gameState)) {
      character match {
        case ghost: Ghost if collisions.exists(_._2 eq ghost) => killGhost(ghost)
        case _ => character
      }
    } else {
      character match {
        case pacman: Pacman if collisions.exists(pacmanCollideGhost(pacman)) => killPacman(pacman)
        case _ => character
      }
    }
  }

  private def ghostCanBeKilled(gameState: GameState) = gameState.ghostInFear

  /**
   * Calcola la velocità dei personaggi.
   * Se pacman è empowered aumenta di velocità mentre diminuisce quella dei fantasmi.
   * Diminuisce sempre la velocità dei fantasmi che si trovano nel tunnel.
   *
   * @param characters Lista dei personaggi in gioco
   * @param gameState  Stato della partita
   * @param collisions Collisioni correnti
   * @param map        Mappa di gioco
   * @return
   */
  def calculateSpeeds(characters: List[Character], level: Int, gameState: GameState)
                     (implicit collisions: List[(Character, GameObject)], map: Map): List[Character] =
    characters.map(char => calculateSpeed(char, level, calculateSpeedCondition(char, gameState)))

  private def calculateSpeed(character: Character, level: Int, speedCondition: SpeedCondition)
                            (implicit collisions: List[(Character, GameObject)], map: Map): Character =
    character match {
      case p@Pacman(_, _, speed, _, _) => if (speed == pacmanSpeed(level, speedCondition)) p else p.copy(speed = pacmanSpeed(level, speedCondition))
      case g@Ghost(_, _, speed, _, _) => if (speed == ghostSpeed(level, speedCondition)) g else g.copy(speed = ghostSpeed(level, speedCondition))
      case _ => character
    }

  private def calculateSpeedCondition(character: Character, gameState: GameState)
                                     (implicit collisions: List[(Character, GameObject)], map: Map): SpeedCondition.Value =
    character match {
      case _: Pacman if gameState.pacmanEmpowered => SpeedCondition.FRIGHT
      case _: Ghost if character.tile.isInstanceOf[Tile.TrackTunnel] => SpeedCondition.TUNNEL
      case _: Ghost if gameState.ghostInFear => SpeedCondition.FRIGHT
      case _ => SpeedCondition.NORM
    }

  /**
   * Calcola lo stato del livello corrente aggiornando di conseguenza lo stato del gioco.
   * Definisce se il livello è in corso oppure se è terminato con la vittoria o la sconfitta del giocatore.
   * @param gameState Stato del gioco
   * @param characters Personaggi in gioco
   * @param map Mappa di gioco
   * @return Stato del gioco aggiornato
   */
  def calculateLevelState(gameState: GameState, characters: List[Character], map: Map): GameState = gameState.copy(
    levelState = calculateLevelState(characters.collect { case p: Pacman => p }, map.dots)
  )

  private def calculateLevelState(pacmans: List[Pacman], dots: Seq[((Int, Int), Dot.Dot)]): LevelState = (pacmans, dots) match {
    case (_, dots) if dots.isEmpty => LevelState.VICTORY
    case (pacmans, _) if pacmans forall (_.isDead == true) => LevelState.DEFEAT
    case _ => LevelState.ONGOING
  }

  def consumeTimeOfGameEvents(gameEvents: List[GameTimedEvent[Any]], timeMs: FiniteDuration): List[GameTimedEvent[Any]] =
    consumeTimeOfGameEvents(gameEvents, timeMs.toMillis)

  /**
   * Riduce il timer temporale degli eventi che lo hanno.
   * @param gameEvents Eventi di gioco temporizzati
   * @param timeMs tempo in millisecondi
   * @return Gli eventi aggiornati
   */
  def consumeTimeOfGameEvents(gameEvents: List[GameTimedEvent[Any]], timeMs: Double): List[GameTimedEvent[Any]] =
    gameEvents.map(event => event.copy(
      timeMs = event.timeMs.map(_ - timeMs),
    ))

  /**
   * Filtra gli eventi il cui timer è scaduto.
   * @param gameEvents Eventi di gioco
   * @param map Mappa di gioco
   * @return Eventi di gioco ancora attivi
   */
  def removeTimedOutGameEvents(gameEvents: List[GameTimedEvent[Any]])(implicit map: Map): List[GameTimedEvent[Any]] =
    gameEvents.filter(!filterTimedOutEvent(map)(_))

  /**
   * Aggiunge nuovi eventi temporizzati in base ad eventuali eventi il cui timer è scaduto, allo stato del gioco
   * e alle collisioni avvenute.
   * @param gameEvents Eventi di gioco
   * @param gameState Stato del gioco
   * @param collisions Collisioni
   * @param map Mappa di gioco
   * @return Nuovi eventi uniti agli eventi in input
   */
  def updateEvents(
                    gameEvents: List[GameTimedEvent[Any]],
                    gameState: GameState,
                    collisions: List[(Character, GameObject)]
                  )
                  (implicit map: Map): List[GameTimedEvent[_]] = {
    val FRUIT_TIMER = 9000
    def chainedEvent(gameEvent: GameTimedEvent[_]): Option[GameTimedEvent[_]] = gameEvent match {
      case GameTimedEvent(FRUIT_SPAWN, _, _, _) => Some(GameTimedEvent(FRUIT_STOP, timeMs = Some(FRUIT_TIMER)))
      case _ => None
    }

    def energizerStop: Option[GameTimedEvent[_]] =
      if (isEnergizerEaten(collisions)) {
        Some(GameTimedEvent(ENERGIZER_STOP, timeMs = Some(Level.energizerDuration(gameState.levelNumber))))
      } else {
        None
      }

    def ghostResume: List[Option[GameTimedEvent[_]]] =
      if (ghostCanBeKilled(gameState)) {
        collisions.map(_._2).collect { case ghost: Ghost => ghost } .map(ghost => Some(GameTimedEvent(
          GHOST_RESTART,
          dots = Some(map.dots.size - Level.ghostRespawnDotCounter(gameState.levelNumber, ghost.characterType)),
          payload = Some(ghost.characterType))
        ))
      } else {
        Nil
      }

    gameEvents :::
      (
        gameEvents.filter(filterTimedOutEvent(map)).map(chainedEvent) :::
          ghostResume :::
          energizerStop :: Nil
        ).filter(_ isDefined).map(_ get)
  }

  /**
   * Esegue le azioni dovute agli eventi il cui timer è scaduto e relative ai personaggi.
   * Nello specifico fa uscire i fantasmi dalla relativa "casa dei fantasmi".
   *
   * @param gameEvents Eventi di gioco
   * @param characters Personaggi in gioco
   * @return I personaggi aggiornati
   */
  def handleEvents(gameEvents: List[GameTimedEvent[Any]], characters: List[Character])(implicit map: Map): List[Character] = {
    def handleEvent(gameEvent: GameTimedEvent[Any], characters: List[Character])(implicit map: Map): List[Character] = gameEvent match {
      case GameTimedEvent(GHOST_RESTART, _, _, Some(ghostType: GhostType)) => characters.map {
        case ghost: Ghost if ghost.characterType == ghostType => ghost.copy(isDead = false)
        case character: Character => character
      }
      case _ => characters
    }

    gameEvents.filter(filterTimedOutEvent(map))
      .foldRight(characters)(handleEvent)
  }

  /**
   * Esegue le azioni dovute agli eventi il cui timer è scaduto e relative alla mappa
   * Nello specifico crea o distrugge i frutti.
   *
   * @param gameEvents Eventi di gioco
   * @param map Mappa di gioco
   * @return La mappa aggiornata
   */
  def handleEvents(gameEvents: List[GameTimedEvent[Any]], map: Map): Map = {
    def handleEvent(gameEvent: GameTimedEvent[Any], map: Map): Map = gameEvent match {
      case GameTimedEvent(FRUIT_SPAWN, _, _, fruit@Some(Fruit.Fruit(_))) => map.putEatable(
        Map getFruitMapIndexes map.mapType,
        fruit.asInstanceOf[Some[Fruit.Fruit]]
      )
      case GameTimedEvent(FRUIT_STOP, _, _, _) => map empty Map.getFruitMapIndexes(map.mapType)
      case _ => map
    }

    gameEvents.filter(filterTimedOutEvent(map))
      .foldRight(map)(handleEvent)
  }

  /**
   * Esegue le azioni dovute agli eventi il cui timer è scaduto e relative alla mappa
   * Nello specifico aggiorna ghostInFear e pacmanEmpowered allo scadere dell'effetto dell'energizer.
   *
   * @param gameEvents Eventi di gioco
   * @param gameState Lo stato di gioco
   * @return Lo stato di gioco aggiornato
   */
  def handleEvents(gameEvents: List[GameTimedEvent[Any]], gameState: GameState)(implicit map: Map): GameState = {
    def handleEvent(gameEvent: GameTimedEvent[Any], gameState: GameState): GameState = gameEvent match {
      case GameTimedEvent(ENERGIZER_STOP, _, _, _) => gameState.copy(ghostInFear = false, pacmanEmpowered = false)
      case _ => gameState
    }

    gameEvents.filter(filterTimedOutEvent(map))
      .foldRight(gameState)(handleEvent)
  }

  private def filterTimedOutEvent(map: Map)(gameEvent: GameTimedEvent[Any]): Boolean =
    !gameEvent.timeMs.exists(_ > 0) && !gameEvent.dots.exists(_ <= map.dots.size)

}
