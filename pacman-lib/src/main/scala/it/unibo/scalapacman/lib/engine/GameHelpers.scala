package it.unibo.scalapacman.lib.engine

import it.unibo.scalapacman.lib.engine.CircularMovement.{moveFor, moveUntil}
import it.unibo.scalapacman.lib.math.{Point2D, TileGeography, Vector2D}
import it.unibo.scalapacman.lib.model.{Character, Direction, Dot, Eatable, Fruit, Map, Tile}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.Direction.{EAST, NORTH, SOUTH, WEST}
import it.unibo.scalapacman.lib.model.Map.MapIndexes
import it.unibo.scalapacman.lib.model.Tile.{Track, TrackSafe}

import scala.reflect.ClassTag

object GameHelpers {

  implicit class CharacterHelper(character: Character)(implicit map: Map) {
    /**
     * Applica la giusta funzione di copy in base all'istanza dell'oggetto character.
     * @param position Nuova posizione. Opzionale.
     * @param speed Nuova velocità. Opzionale.
     * @param direction Nuova direzione. Opzionale.
     * @param isDead Nuovo stato isDead. Opzionale.
     * @return Una copia aggiornata dell'oggetto character
     */
    def copy(
              position: Point2D = character.position,
              speed: Double = character.speed,
              direction: Direction = character.direction,
              isDead: Boolean = character.isDead
            ): Character = Character.copy(character)(position, speed, direction, isDead)

    /**
     * Inverte la direzione del personaggio
     * @return Copia del personaggio con la direzione invertita
     */
    def revert: Character = character.direction match {
      case EAST | WEST | NORTH | SOUTH => copy(direction = character.direction.reverse)
      case _ => character
    }

    /**
     * Definisce se il personaggio sta desiderando di invertire la propria direzione corrente oppure no.
     * @param desiredDirection Direzione desiderata
     * @return true se il personaggio possiede una direzione inversa rispetto alla direzione
     *         desiderata, false altrimenti.
     */
    def desireRevert(desiredDirection: Direction): Boolean = character.direction match {
      case EAST if desiredDirection == WEST => true
      case WEST if desiredDirection == EAST => true
      case NORTH if desiredDirection == SOUTH => true
      case SOUTH if desiredDirection == NORTH => true
      case _ => false
    }

    /**
     * Calcola il punto centrale di una Tile più prossimo, considerando che il personaggio
     * si muova verso la propria direzione corrente.
     * @param map Mappa di gioco
     * @return Il punto centrale più prossimo
     */
    def nextTileCenter(implicit map: Map): Point2D =
      (tileOrigin :: nextTileOrigin :: Nil)
        .map(_ + TileGeography.center)
        .minBy(moveUntil(character, _))

    /**
     * Cambia la direzione corrente del personaggio in quella desiderata solo se in tale direzione è presente
     * una tile in cui può muoversi.
     * @param desiredDirection Direzione desiderata
     * @param map Mappa di gioco
     * @return Il personaggio con la nuova direzione se possibile, altrimenti lo stesso oggetto immutato.
     */
    def changeDirectionIfPossible(desiredDirection: Direction)(implicit map: Map): Character =
      if (character.direction != desiredDirection && nextTile(desiredDirection).walkable(character)) {
        copy(direction = desiredDirection)
      } else {
        character
      }

    /**
     * Se la prossima tile di fronte al personaggio è camminabile si sposta in tale direzione, altrimenti
     * il personaggio rimane immutato.
     * @param timeMs Tempo di spostamento in millisecondi
     * @param map Mappa di gioco
     * @return Il personaggio con la nuova posizione se è possibile spostarsi, altrimenti lo stesso oggetto immutato.
     */
    def moveIfPossible(timeMs: Double)(implicit map: Map): Character = if (nextTile.walkable(character)) {
      copy(position = moveFor(character, timeMs))
    } else {
      character
    }

    /**
     * La posizione di origine della tile corrente.
     * @return La posizione di origine
     */
    def tileOrigin: Point2D = map.tileOrigin(character.position)

    /**
     * La posizione di origine della prossima tile nella direzione corrente del personaggio.
     * @return La posizione di origine
     */
    def nextTileOrigin: Point2D = map.tileOrigin(character.position, Some(character.direction).map(CharacterMovement.vector))

    /**
     * La tile corrente
     * @return La tile corrente
     */
    def tile: Tile = map.tile(character.position)

    /**
     * La prossima tile nella direzione corrente del personaggio
     * @return La prossima tile
     */
    def nextTile: Tile = map.tile(character.position, Some(character.direction).map(CharacterMovement.vector))

    /**
     * La prossima tile calcolata dalla tile corrente verso la direzione richiesta
     * @param direction La direzione richiesta
     * @return La prossima tile
     */
    def nextTile(direction: Direction): Tile = map.tile(character.position, Some(direction).map(CharacterMovement.vector))

    /**
     * Gli indici della prossima tile calcolata dalla tile corrente verso la direzione richiesta
     * @param direction La direzione richiesta
     * @param tileIndexes Gli indici della tile corrente
     * @return Gli indici della prossima tile
     */
    def nextTileIndexes(direction: Direction, tileIndexes: MapIndexes): MapIndexes =
      map.tileIndexes(Point2D(tileIndexes._1 * TileGeography.SIZE, tileIndexes._2 * TileGeography.SIZE), Some(direction).map(CharacterMovement.vector))

    /**
     * Gli indici della tile corrente del personaggio
     * @return Gli indici della prossima tile
     */
    def tileIndexes: MapIndexes = map.tileIndexes(character.position)

    /**
     * Rimuove l'oggetto presente nella tile corrente del personaggio, se presente.
     * @return La mappa aggiornata
     */
    def eat: Map = map.empty(character.tileIndexes)

    /**
     * Verifica se la tile corrente è un incrocio a tre o quattro vie per questo personaggio
     * @return true se è un incrocio, false altrimenti
     */
    def tileIsCross: Boolean = tileIsCross(tileIndexes)

    /**
     * Verifica se la tile richiesta è un incrocio a tre o quattro vie per questo personaggio
     * @param tileIndexes Indici della tile richiesta
     * @return true se è un incrocio, false altrimenti
     */
    def tileIsCross(tileIndexes: MapIndexes): Boolean = map.tileIsCross(tileIndexes, character)

    /**
     * Cerca la prossima tile ad incrocio spostandosi nella direzione corrente
     * fino al prossimo muro.
     * @return Gli indici della prossima tile ad incrocio se presente, None altrimenti
     */
    def nextCrossTile(): Option[MapIndexes] = nextCrossTile(character.tileIndexes, character.direction)

    /**
     * A partire dalla tile in input, cerca la prossima tile ad incrocio spostandosi linearmente nella direzione
     * richiesta fino al prossimo muro.
     * @param tileIndexes Indici della tile da cui fare la ricerca
     * @param direction Direzione verso cui fare ricerca
     * @return Gli indici della prossima tile ad incrocio se presente, None altrimenti
     */
    def nextCrossTile(tileIndexes: MapIndexes, direction: Direction): Option[MapIndexes] =
      untilWall(tileIndexes, direction) match {
        case Some(x) if tileIsCross(x._2) => Some(x._2)
        case Some(x) => nextCrossTile(x._2, x._1)
        case None => None
      }

    def directionForTurn: Option[Direction] = directionForTurn(character.direction)

    def directionForTurn(dir: Direction): Option[Direction] = directionForTurn(character.tileIndexes, dir)

    /**
     * A partire dalla tile richiesta, indica in quale direzione è possibile trovare una tile camminabile.
     * Non ritorna mai la tile in direzione inversa rispetto a quella in input.
     * @param tileIndexes Indici della tile da cui fare la ricerca
     * @param direction Direzione verso cui fare ricerca
     * @return La direzione verso cui si trova la prossima tile camminabile, a meno che non ci si trovi in un vicolo cieco.
     */
    def directionForTurn(tileIndexes: MapIndexes, direction: Direction): Option[Direction] =
      untilWall(tileIndexes, direction) match {
        case Some(x) if x._1 == direction => directionForTurn(x._2, x._1)
        case Some(x) => Some(x._1)
        case None => None
      }

    /**
     * A partire dalla tile richiesta, cerca la tile camminabile immediatamente seguente.
     * Vengono controllate tutte le direzioni tranne quella inversa rispetto alla direzione in input.
     * In caso di più tile a disposizione preferisce quella nella direzione in input, poi quella alla sua destra
     * e infine quella alla sua sinistra.
     * @param tileIndexes Indici della tile da cui fare la ricerca
     * @param direction Direzione verso cui fare ricerca
     * @return La direzione e gli indici della prossima tile camminabile, a meno che non ci si trovi in un vicolo cieco.
     */
    private def untilWall(tileIndexes: MapIndexes, direction: Direction): Option[(Direction, MapIndexes)] =
      List(direction, direction.sharpTurnRight, direction.sharpTurnLeft)
        .map(dir => (dir, nextTileIndexes(dir, tileIndexes)))
        .find(x => map.tile(x._2).walkable(character))
  }

  implicit class MapHelper(map: Map) {
    val height: Int = map.tiles.size
    val width: Int = map.tiles.head.size

    /**
     * Dato un punto X e un'intensità di spostamento sulla relativa asse, calcola
     * l'indice della tile di una ipotetica mappa contenente il punto derivato dall'unione degli input.
     * Non conoscendo le dimensioni della mappa, non tiene conto dell'effetto Pacman.
     * @param x Un determinato punto X
     * @param watchOut Intensità di spostamento sull'asse del punto X
     * @return L'indice della tile in una ipotetica mappa
     */
    private def tileIndex(x: Double, watchOut: Option[Double] = None): Int = ((x + watchOut.getOrElse(0.0)) / TileGeography.SIZE).floor.toInt

    /**
     * La tile contenente il punto richiesto, eventualmente modificato da un vettore
     * @param position Punto richiesto
     * @param watchOut Vettore che modifica position. Opzionale.
     * @return Tile contenente il punto in input
     */
    def tile(position: Point2D, watchOut: Option[Vector2D] = None): Tile =
      map.tiles(pacmanEffect(tileIndex(position.y, watchOut.map(_.y)), height))(pacmanEffect(tileIndex(position.x, watchOut.map(_.x)), width))

    /**
     * La tile corrispondente agli indici in input.
     * @param indexes Indici della tile
     * @return La tile corrispondente
     */
    def tile(indexes: MapIndexes): Tile = map.tiles(pacmanEffect(indexes._2, height))(pacmanEffect(indexes._1, width))

    /**
     * Indici della tile contenente il punto in input
     * @param position Il punto che deve essere contenuto dalla tile
     * @return Gli indici della tile
     */
    def tileIndexes(position: Point2D): MapIndexes = (
      pacmanEffect(tileIndex(position.x), width),
      pacmanEffect(tileIndex(position.y), height)
    )

    /**
     * Applica l'effetto Pacman agli indici in input per ottenere indici sicuramente
     * interni alla mappa.
     * @param indexes Indici della tile nella mappa
     * @return Gli stessi indici eventualmente rimappati dall'effetto Pacman
     */
    def tileIndexes(indexes: MapIndexes): MapIndexes = (
      pacmanEffect(indexes._1, width),
      pacmanEffect(indexes._2, height)
    )

    /**
     * Indici della tile contenente il punto in input, ottenuto dal punto position trasformato dall'eventuale vettore watchOut.
     * @param position Il punto che deve essere contenuto dalla tile
     * @param watchOut Vettore che modifica position. Opzionale.
     * @return Gli indici della tile
     */
    def tileIndexes(position: Point2D, watchOut: Option[Vector2D] = None): MapIndexes = (
      pacmanEffect(tileIndex(position.x, watchOut.map(_.x)), width),
      pacmanEffect(tileIndex(position.y, watchOut.map(_.y)), height)
    )

    /**
     * Il punto di origine della tile contenente il punto in input, trasformato dall'eventuale vettore watchOut.
     * @param position Punto che deve essere contenuto nella tile
     * @param watchOut Vettore di trasformazione del punto. Opzionale.
     * @return Le coordinate del punto di origine della tile contenente il punto in input
     */
    def tileOrigin(position: Point2D, watchOut: Option[Vector2D] = None): Point2D = Point2D(
      pacmanEffect(tileIndex(position.x, watchOut.map(_.x)), width) * TileGeography.SIZE,
      pacmanEffect(tileIndex(position.y, watchOut.map(_.y)), height) * TileGeography.SIZE
    )

    /**
     * Il punto di origine della tile corrispondente agli indici in input.
     * @param indexes Indici della tile nella mappa
     * @return Le coordinate del punto di origine della tile
     */
    def tileOrigin(indexes: MapIndexes): Point2D = Point2D(
      pacmanEffect(indexes._1, width) * TileGeography.SIZE,
      pacmanEffect(indexes._2, height) * TileGeography.SIZE
    )

    /**
     * Verifica se la tile corrispondente agli indici in input è un incrocio a tre o quattro vie
     * per il personaggio richiesto.
     * @param tileIndexes Indici della tile
     * @param character Personaggio interessato al risultato
     * @return true se è un incrocio, false altrimenti
     */
    def tileIsCross(tileIndexes: MapIndexes, character: Character): Boolean =
      map.tileNeighboursIndexes(tileIndexes).count(map.tile(_).walkable(character)) > 2

    /**
     * Calcola le tile ortogonalmente adiacenti alla tile corrispondente agli indici in input e ne
     * ritorna i relativi indici.
     * @param tileIndexes Indici della tile
     * @return Indici delle tile adiacenti alla tile richiesta
     */
    def tileNeighboursIndexes(tileIndexes: MapIndexes): List[MapIndexes] =
      ((1, 0) :: (-1, 0) :: (0, 1) :: (0, -1) :: Nil)
        .map(p => (p._1 + tileIndexes._1, p._2 + tileIndexes._2))
        .map(map.tileIndexes)

    /**
     * Cerca la prossima tile ad incrocio spostandosi in tutte e quattro le direzioni
     * fino al prossimo muro. Non vengono restituiti i risultati per le direzioni
     * in cui sono presenti muri o altre tile non camminabili per il personaggio in input.
     * @param tileIndexes Indici della tile
     * @param character Personaggio interessato al risultato
     * @return Gli indici degli incroci raggiungibili muovendosi in ogni direzione libera
     */
    def tileNearbyCrossings(tileIndexes: MapIndexes, character: Character): List[MapIndexes] =
      map.tileNeighboursIndexes(tileIndexes)
        .filter(map.tile(_).walkable(character))
        .map((tileIndexes, _))
        .map(Direction.byPath)
        .flatMap(CharacterHelper(character)(map).nextCrossTile(tileIndexes, _))

    /**
     * La prossima tile nella direzione richiesta rispetto alla tile corrispondente agli indici in input
     * @param tileIndexes Indici della tile di partenza
     * @param direction Direzione verso cui calcolare la tile adiacente
     * @return La tile adiacente
     */
    def nextTile(tileIndexes: MapIndexes, direction: Direction): Tile = map.tile(map.tileOrigin(tileIndexes), Some(direction).map(CharacterMovement.vector))

    @scala.annotation.tailrec
    private def pacmanEffect(x: Int, max: Int): Int = x match {
      case x: Int if x >= 0 => x % max
      case x: Int => pacmanEffect(x + max, max)
    }

    /**
     * Rimuove eventuali oggetti presenti nella tile corrispondente agli indici in input.
     * @param indexes Indici della tile
     * @return La mappa aggiornata
     */
    def empty(indexes: MapIndexes): Map = putEatable(indexes, None)

    /**
     * Inserisce l'oggetto richiesto nella tile corrispondente agli indici in input.
     * In caso di None eventuali oggetti vengono rimossi.
     * @param indexes Indici della tile
     * @param option Oggetto da inserire, None per rimuovere eventuali oggetti
     * @return La mappa aggiornata
     */
    def putEatable(indexes: MapIndexes, option: Option[Eatable]): Map = map.copy(
      tiles = map.tiles.updated(indexes._2, putEatableOnRow(indexes._1, map.tiles(indexes._2), option))
    )

    private def putEatableOnRow(index: Int, row: List[Tile], option: Option[Eatable]): List[Tile] =
      row.updated(index, putEatableOnTile(row(index), option))

    private def putEatableOnTile(tile: Tile, option: Option[Eatable]): Tile = tile match {
      case Track(_) | TrackSafe(_) => Track(option)
      case _ => throw new IllegalArgumentException("This tile can't contains an eatable, only Track and TrackSafe")
    }

    /**
     * Ritorna la lista di tutti gli oggetti presenti nelle tile della mappa accoppiati con gli indici
     * della tile in cui si trovano.
     * Indicando come generic una classe diversa da Eatable è possibile filtrare
     * gli oggetti da ritornare.
     * @tparam A Tipo degli oggetti da ritornare, estensione di Eatable
     * @return Lista della coppia con l'oggetto trovato e gli indici della tile in cui si trova
     */
    def eatablesToSeq[A <: Eatable : ClassTag]: Seq[(MapIndexes, A)] =
      for (
        y <- 0 until map.height;
        x <- 0 until map.width;
        eatable <- map.tiles(y)(x).eatable collect { case a: A => a }
      ) yield ((x, y), eatable)

    def dots: Seq[(MapIndexes, Dot.Dot)] = map.eatablesToSeq[Dot.Dot]

    def fruit: Option[(MapIndexes, Fruit.Fruit)] = map.eatablesToSeq[Fruit.Fruit] match {
      case Seq(fruit) => Some(fruit)
      case Seq() => None
    }
  }

}
