package it.unibo.scalapacman.server.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.scalapacman.common.UpdateModelDTO
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.server.core.GhostAct.Setup

object GhostAct {

  private case class Setup(gameId: String,
                           context: ActorContext[Engine.UpdateCommand],
                           engine: ActorRef[Engine.EngineCommand],
                           ghostType: GhostType)

  def apply(id: String, engine: ActorRef[Engine.EngineCommand], ghostType: GhostType): Behavior[Engine.UpdateCommand] =
    Behaviors.setup { context =>
      new GhostAct(Setup(id, context, engine, ghostType)).coreRoutine()
    }
}


private class GhostAct(setup: Setup) {

  setup.engine ! Engine.RegisterGhost(setup.context.self, setup.ghostType)

  private def coreRoutine(): Behaviors.Receive[Engine.UpdateCommand] =
    Behaviors.receiveMessage {
      case Engine.UpdateMsg(newModel) => handleEngineUpdate(newModel)
      case _ => Behaviors.same
    }

  private def handleEngineUpdate(model: UpdateModelDTO): Behavior[Engine.UpdateCommand] ={
    setup.context.log.info("Ricevuto update: " + model)
    Behaviors.same
  }
}

