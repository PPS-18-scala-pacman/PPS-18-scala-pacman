package it.unibo.scalapacman.server.core

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import it.unibo.scalapacman.lib.model.PacmanType
import it.unibo.scalapacman.server.config.ConfLoader
import it.unibo.scalapacman.server.config.TestSettings.{awaitLowerBound, awaitUpperBound}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.reflect.ClassTag

class MasterTest extends ScalaTestWithActorTestKit(ConfLoader.akkaConf) with AnyWordSpecLike {

  private var gameCreatedProbe: TestProbe[Master.GameCreated] = _
  private var masterActor: ActorRef[Master.MasterCommand] = _

  private val defaultComponents = Map(PacmanType.PACMAN.toString()-> PacmanType.PACMAN)

  implicit def stringToKeyService[A: ClassTag](keyId: String): ServiceKey[A] = ServiceKey[A](keyId)

  override def beforeAll(): Unit = {
    super.beforeAll()

    gameCreatedProbe = createTestProbe[Master.GameCreated]()
    masterActor = spawn(Master())
  }

  def findActors[A: ClassTag](key: ServiceKey[A]): Set[ActorRef[A]] = {
    val gameProbe = createTestProbe[Receptionist.Listing]()
    var actors = Set[ActorRef[A]]()

    TestProbe().awaitAssert({
      system.receptionist ! Receptionist.Find(key, gameProbe.ref)
      actors = gameProbe.receiveMessage().serviceInstances(key)
      actors should not be null // scalastyle:ignore null
      actors.size should be > 0
    }, awaitUpperBound, awaitLowerBound)
    actors
  }

  "A Master actor" must {

    "is discoverable" in {

      val res = findActors(Master.masterServiceKey)
      res should have size 1
    }

    "is able to start a game actor" in {
      masterActor ! Master.CreateGame(gameCreatedProbe.ref, defaultComponents)
      val gameId = gameCreatedProbe.receiveMessage().gameId
      val res = findActors[Game.GameCommand](gameId)
      res should have size 1
    }

    "spawn different actors each times" in {

      masterActor ! Master.CreateGame(gameCreatedProbe.ref, defaultComponents)
      val gameIdFst = gameCreatedProbe.receiveMessage().gameId
      val resFst = findActors[Game.GameCommand](gameIdFst)
      resFst should have size 1
      val gameFst = resFst.head

      masterActor ! Master.CreateGame(gameCreatedProbe.ref, defaultComponents)
      val gameIdSnd = gameCreatedProbe.receiveMessage().gameId
      val resSnd = findActors[Game.GameCommand](gameIdSnd)
      resSnd should have size 1
      val gameSnd = resSnd.head

      gameFst.path should !==(gameSnd.path)
    }
  }
}
