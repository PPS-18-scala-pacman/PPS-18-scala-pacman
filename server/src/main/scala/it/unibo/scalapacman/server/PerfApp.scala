package it.unibo.scalapacman.server

import java.util.concurrent.Semaphore

import it.unibo.scalapacman.common.{GameCharacter, UpdateModelDTO}
import it.unibo.scalapacman.lib.ai.GhostAI
import it.unibo.scalapacman.lib.math.{Point2D, TileGeography}
import it.unibo.scalapacman.lib.model.Character.Ghost
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.GhostType.{BLINKY, CLYDE, INKY, PINKY}
import it.unibo.scalapacman.lib.model.{GhostType, PacmanType}
import it.unibo.scalapacman.server.core.{Engine, Game}
import it.unibo.scalapacman.server.model.{GameEntity, GameParameter, GameParticipant}

import scala.util.Random
//scalastyle:off
object PerfApp {

  def step1(model: Engine.Model, setup: Engine.Setup): UpdateModelDTO = {
    val gameModel = Engine.computeUpdate(model, setup)
    Engine.elaborateUpdateModel(gameModel.data, paused = false)
  }

  def step2(nickname: String, model: UpdateModelDTO): Option[Direction] = {
    val selfDTO = model.gameEntities.find(_.id == nickname)
    val pacmanTypes = GameCharacter.PACMAN :: GameCharacter.MS_PACMAN :: GameCharacter.CAPMAN :: GameCharacter.RAPMAN :: Nil;
    val pacmanList = model.gameEntities
      .filter(e => pacmanTypes.contains(e.gameCharacterHolder.gameChar))
      .map(_.toPacman.get)

    val self = selfDTO.get.toGhost.get
    val pacman = GhostAI.choosePacmanToFollow(self, pacmanList)
    GhostAI.calculateDirectionClassic(self, pacman)
  }

  def sequential(dto: UpdateModelDTO, ghostList :List[GameEntity]): Unit = {
    ghostList.foreach(gt => PerfApp.step2(gt.nickname, dto))
  }
  def concurrent(dto: UpdateModelDTO, ghostList :List[GameEntity]): Unit = {
    val sem = new Semaphore(0)
    ghostList.foreach( gt => {
      val r = new MyThread(gt.nickname, sem, dto)
      val thread = new Thread(r)
      thread.start()
    })
    sem.acquire(4)
  }

  def testSingolo() = {
    // inizializzazione attori partecipanti
    val components: Map[String, PacmanType.PacmanType] = Map("pac" -> PacmanType.PACMAN, "cap" -> PacmanType.CAPMAN)
    val defaultGhosts = List(BLINKY, INKY, PINKY, CLYDE)
    implicit val generator: Random = new Random(System.currentTimeMillis())
    val ghostList = defaultGhosts.map(gt => GameEntity(Game.generateGhostId(gt, components.keys.toList), gt))
    val entityList: List[GameEntity] = ghostList ::: components.map(cp => GameEntity(cp._1, cp._2)).toList

    val setup = Engine.Setup("", null, GameParameter(entityList, 1))
    val model = Engine.initEngineModel(Set(), Set(), setup)

    val positionedPar = model.data.participants.map(p => p.character match {
      case c@Ghost(GhostType.CLYDE, _, _, _, _) => p.copy( character = c.copy(position = Point2D(1 * TileGeography.SIZE, 1 * TileGeography.SIZE) + TileGeography.center, isDead = false))
      case c@Ghost(GhostType.INKY, _, _, _, _)  => p.copy( character = c.copy(position = Point2D(26 * TileGeography.SIZE, 28 * TileGeography.SIZE) + TileGeography.center, isDead = false))
      case c@Ghost(GhostType.PINKY, _, _, _, _) => p.copy( character = c.copy(position = Point2D(15 * TileGeography.SIZE, 3 * TileGeography.SIZE) + TileGeography.center, isDead = false))
      case _ => p
    })
    model.copy(data = model.data.copy(participants = positionedPar))

    // fine inizializzazione

    println("Inizio esecuzione")
    val t1 = System.currentTimeMillis()

    val dto = PerfApp.step1(model, setup)

    val t2 = System.currentTimeMillis()

    //PerfApp.sequential(dto, ghostList)
    PerfApp.concurrent(dto, ghostList)

    val t3 = System.currentTimeMillis()

    val step1: Double = (t2 - t1) / 1000.0
    val step2: Double = (t3 - t2) / 1000.0
    val tot = step1 + step2

    println("step1 " + step1)
    println("step2 " + step2)
    println("tempo esecuzione = " + tot)
  }

  def testMulti(num: Int): Unit = {

    // inizializzazione attori partecipanti
    val components: Map[String, PacmanType.PacmanType] = Map("pac" -> PacmanType.PACMAN, "cap" -> PacmanType.CAPMAN)
    val defaultGhosts = List(BLINKY, INKY, PINKY, CLYDE)
    implicit val generator: Random = new Random(System.currentTimeMillis())
    val ghostList = defaultGhosts.map(gt => GameEntity(Game.generateGhostId(gt, components.keys.toList), gt))
    val entityList: List[GameEntity] = ghostList ::: components.map(cp => GameEntity(cp._1, cp._2)).toList

    val setup = Engine.Setup("", null, GameParameter(entityList, 1))
    val model = Engine.initEngineModel(Set(), Set(), setup)

    val positionedPar = model.data.participants.map(p => p.character match {
      case c@Ghost(GhostType.CLYDE, _, _, _, _) => p.copy( character = c.copy(position = Point2D(1 * TileGeography.SIZE, 1 * TileGeography.SIZE) + TileGeography.center, isDead = false))
      case c@Ghost(GhostType.INKY, _, _, _, _)  => p.copy( character = c.copy(position = Point2D(26 * TileGeography.SIZE, 28 * TileGeography.SIZE) + TileGeography.center, isDead = false))
      case c@Ghost(GhostType.PINKY, _, _, _, _) => p.copy( character = c.copy(position = Point2D(15 * TileGeography.SIZE, 3 * TileGeography.SIZE) + TileGeography.center, isDead = false))
      case _ => p
    })
    model.copy(data = model.data.copy(participants = positionedPar))

    // fine inizializzazione

    println("Inizio esecuzione")

    val res: IndexedSeq[(Long, Long, Long)] = (0 until num).map(_ => {
      val t1 = System.currentTimeMillis()

      val dto = PerfApp.step1(model, setup)

      val t2 = System.currentTimeMillis()

      //PerfApp.sequential(dto, ghostList)
      PerfApp.concurrent(dto, ghostList)

      val t3 = System.currentTimeMillis()
      (t1, t2, t3)
    })

    val op: ((Long, Long, Long), (Long, Long, Long)) => (Long, Long, Long) =
      (curRes, cum) => (curRes._1 + cum._1, curRes._2 + cum._2, curRes._3 + cum._3)

    val totTimes: (Long, Long, Long) = res.fold( (0L,0L,0L) )( op )
    val avgTimes: (Double, Double, Double) = (totTimes._1 / num, totTimes._2 / num, totTimes._3 / num)

    val step1: Double = (avgTimes._2 - avgTimes._1) / 1000.0
    val step2: Double = (avgTimes._3 - avgTimes._2) / 1000.0
    val tot = step1 + step2

    println("step1 " + step1)
    println("step2 " + step2)
    println("tempo esecuzione = " + tot)
  }
}

class MyThread(nickname: String, sem: Semaphore, dto: UpdateModelDTO) extends Runnable {
  def run {
    //val t1 = System.currentTimeMillis()
    PerfApp.step2(nickname, dto)
    //val t2 = System.currentTimeMillis()
    sem.release()
    /*val tot: Double = (t2 - t1) / 1000.0
    println(s"tempo esecuzione $nickname = " + tot)*/
  }
}

object RunPerf extends App {
  PerfApp.testMulti(100)
}
