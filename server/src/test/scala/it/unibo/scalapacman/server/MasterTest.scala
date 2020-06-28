package it.unibo.scalapacman.server

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import it.unibo.scalapacman.server.Game.GameCommand
import it.unibo.scalapacman.server.Master.GameCreated
import org.scalatest.wordspec.AnyWordSpecLike

class MasterTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Master actor" must {

    "be able to start a game actor" in {

      val masterProbe = createTestProbe[GameCreated]()
      val masterActor = spawn(Master())

      masterActor ! Master.CreateGame(masterProbe.ref)
      val gameIdFst = masterProbe.receiveMessage().gameId
      val gameProbeFst = createTestProbe[Receptionist.Listing]()
      val keyFst = ServiceKey[GameCommand](gameIdFst)
      system.receptionist ! Receptionist.Find(keyFst, gameProbeFst.ref)
      val resFst:Set[ActorRef[Game.GameCommand]] = gameProbeFst.receiveMessage().serviceInstances(keyFst)
      resFst.size should be (1)
      val gameFst = resFst.head

      masterActor ! Master.CreateGame(masterProbe.ref)
      val gameIdSnd = masterProbe.receiveMessage().gameId
      val gameProbeSnd = createTestProbe[Receptionist.Listing]()
      val keySnd = ServiceKey[GameCommand](gameIdSnd)
      system.receptionist ! Receptionist.Find(ServiceKey[GameCommand](gameIdSnd), gameProbeSnd.ref)
      val resSnd = gameProbeSnd.receiveMessage().serviceInstances(keySnd)
      resSnd.size should be (1)
      val gameSnd = resSnd.head

      gameFst should !==(gameSnd)
    }
  }
}
