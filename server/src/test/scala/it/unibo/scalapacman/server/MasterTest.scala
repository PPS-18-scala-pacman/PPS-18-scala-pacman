package it.unibo.scalapacman.server

import java.util.concurrent.TimeUnit

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag

class MasterTest extends ScalaTestWithActorTestKit with AnyWordSpecLike with BeforeAndAfterAll {

  private var masterProbe: TestProbe[Master.GameCreated] = _
  private var masterActor: ActorRef[Master.MasterCommand] = _

  def findActors[A: ClassTag](keyId: String): Set[ActorRef[A]] = {
    val gameProbe = createTestProbe[Receptionist.Listing]()
    val key = ServiceKey[A](keyId)
    var actors: Set[ActorRef[A]] = null

    masterProbe.awaitAssert({
      system.receptionist ! Receptionist.Find(key, gameProbe.ref)
      actors = gameProbe.receiveMessage().serviceInstances(key)
      actors should not be (null)
      actors.size should be > (0)
    }, FiniteDuration(5, TimeUnit.SECONDS), FiniteDuration(250, TimeUnit.MILLISECONDS))
    actors
  }

  override def beforeAll(): Unit = {
    masterProbe = createTestProbe[Master.GameCreated]()
    masterActor = spawn(Master())
  }

  "Master actor" must {

    "be able to start a game actor" in {

      masterActor ! Master.CreateGame(masterProbe.ref)
      val gameId = masterProbe.receiveMessage().gameId
      val res = findActors[Game.GameCommand](gameId)
      res should have size 1
    }

    "spawn different actors each times" in {

      masterActor ! Master.CreateGame(masterProbe.ref)
      val gameIdFst = masterProbe.receiveMessage().gameId
      val resFst = findActors[Game.GameCommand](gameIdFst)
      resFst should have size 1
      val gameFst = resFst.head

      masterActor ! Master.CreateGame(masterProbe.ref)
      val gameIdSnd = masterProbe.receiveMessage().gameId
      val resSnd = findActors[Game.GameCommand](gameIdSnd)
      resSnd should have size 1
      val gameSnd = resSnd.head

      gameFst.path should !==(gameSnd.path)
    }
  }
}
