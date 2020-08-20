package it.unibo.scalapacman.server.core

import akka.actor.typed.{ActorRef, Behavior, ChildFailed, MailboxSelector}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.model.ws.Message
import it.unibo.scalapacman.lib.model.GhostType
import it.unibo.scalapacman.server.core.Engine.EngineCommand
import it.unibo.scalapacman.server.core.Game.{CloseCommand, GameCommand, RegisterPlayer, Setup}
import it.unibo.scalapacman.server.core.Player.{PlayerCommand, PlayerRegistration, RegistrationRejected}

object Game {

  sealed trait GameCommand
  case class CloseCommand() extends GameCommand

  //FIXME fare un wrapper per il messaggio identico su player? lascciare cosi? fare che handler scrive direttamente a Player
  // o che noi gli diciamo chi è player? (forse l'ultima è la meglio con un getFreePlayer?)
  case class RegisterPlayer(replyTo: ActorRef[PlayerRegistration], source: ActorRef[Message]) extends GameCommand

  //FIXME fare un model oltre al setup???
  private case class Setup(id: String,
                           context: ActorContext[GameCommand],
                           engine: ActorRef[EngineCommand],
                           player: ActorRef[PlayerCommand])

  def apply(id: String): Behavior[GameCommand] =
    Behaviors.setup { context =>

      val gameServiceKey: ServiceKey[GameCommand] = ServiceKey[GameCommand](id)
      context.system.receptionist ! Receptionist.Register(gameServiceKey, context.self)

      val engine = context.spawn(Engine(id, 1), "EngineActor")
      context.watch(engine)
      val player = context.spawn(Player(id, engine), "PlayerActor")
      context.watch(player)

      val props = MailboxSelector.fromConfig("server-app.ghost-mailbox")
      context.spawn(GhostAct(id, engine, GhostType.PINKY), "PinkyActor", props)
      context.spawn(GhostAct(id, engine, GhostType.BLINKY), "BlinkyActor", props)
      context.spawn(GhostAct(id, engine, GhostType.INKY), "InkyActor", props)
      context.spawn(GhostAct(id, engine, GhostType.CLYDE), "ClydeActor", props)

      new Game(Setup(id, context, engine, player)).initRoutine()
        .receiveSignal {
          case (context, ChildFailed(`engine`, _)) =>
            context.log.info("Engine stopped")
            //TODO notificare gli elementi interessati che il game verrà chiuso
            Behaviors.stopped
          case (context, ChildFailed(`player`, _)) =>
            context.log.info("Player stopped")
            engine ! Engine.Pause()
            //TODO deregistrare player su engine e crearne uno nuovo
            Behaviors.same
          case (context, ChildFailed(_, _)) =>
            context.log.info("Ghost stopped")
            engine ! Engine.Pause()
            //TODO deregistrare ghost su engine e crearne uno nuovo
            Behaviors.same
        }
    }
}

private class Game(setup: Setup) {

  private def initRoutine(): Behaviors.Receive[Game.GameCommand] =
    Behaviors.receiveMessage {
      case CloseCommand() => close()
      case RegisterPlayer(replyTo, source) =>
        setup.context.log.info("RegisterPlayer ricevuto")
        setup.player ! Player.RegisterUser(replyTo, source)
        coreRoutine()
    }

  private def coreRoutine(): Behaviors.Receive[Game.GameCommand] =
    Behaviors.receiveMessage {
      case CloseCommand() => close()
      case RegisterPlayer(replyTo, _) =>
        replyTo ! RegistrationRejected("Gioco in corso")
        Behaviors.same
    }

  private def close(): Behavior[GameCommand] = {
    setup.context.log.info("CloseCommand ricevuto")
    Behaviors.stopped
  }

}
