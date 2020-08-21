package it.unibo.scalapacman.server.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.scalapacman.common.{GameCharacter, UpdateModelDTO}
import it.unibo.scalapacman.lib.ai.GhostAI
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.lib.model.{GameState, LevelState}
import it.unibo.scalapacman.server.core.Engine.ChangeDirectionReq
import it.unibo.scalapacman.server.core.GhostAct.{Model, Setup}
import it.unibo.scalapacman.server.model.MoveDirection.directionToMoveDirection
import it.unibo.scalapacman.server.model.MoveDirection.MoveDirection

object GhostAct {

  private case class Setup(gameId: String,
                           context: ActorContext[Engine.UpdateCommand],
                           engine: ActorRef[Engine.EngineCommand],
                           ghostType: GhostType)

  case class Model(state: GameState, desMove: Option[MoveDirection])

  def apply(id: String, engine: ActorRef[Engine.EngineCommand], ghostType: GhostType): Behavior[Engine.UpdateCommand] =
    Behaviors.setup { context =>
      new GhostAct(Setup(id, context, engine, ghostType)).coreRoutine(Model(GameState(0), None))
    }
}

private class GhostAct(setup: Setup) {

  setup.context.log.info(s"GhostActor per fantasma ${setup.ghostType}")
  setup.engine ! Engine.RegisterGhost(setup.context.self, setup.ghostType)

  private def coreRoutine(model: Model): Behaviors.Receive[Engine.UpdateCommand] =
    Behaviors.receiveMessage {
      case Engine.UpdateMsg(newModel) => handleEngineUpdate(newModel, model)
      case _ => Behaviors.same
    }

  private def handleEngineUpdate(model: UpdateModelDTO, myModel: Model): Behavior[Engine.UpdateCommand] ={
    setup.context.log.debug("Ricevuto update: " + model)
    val gameState: GameState = model.state

    if(gameState.levelState != LevelState.ONGOING) {
      setup.context.log.info("Partita terminata spegnimento")
      Behaviors.stopped
    } else {

      val selfDTO = model.gameEntities.find(_.gameCharacterHolder.gameChar == GameCharacter.ghostTypeToGameCharacter(setup.ghostType))
      val pacmanDTO = model.gameEntities.find(_.gameCharacterHolder.gameChar == GameCharacter.PACMAN)

      if (selfDTO.isDefined && pacmanDTO.isDefined) {

        val pacman = pacmanDTO.get.toPacman.get
        val direction: Option[Direction] = GhostAI.calculateDirectionClassic(selfDTO.get.toGhost.get, pacman)
        //val direction = GhostAI.desiredDirection(selfDTO.get.toGhost.get, pacmanDTO.get.toPacman.get)(prologEngine, updatedMap)

        if (direction.isDefined) {
          if (!myModel.desMove.contains(direction.get)) setup.engine ! ChangeDirectionReq(setup.context.self, direction.get)

          coreRoutine(Model(gameState, Some(direction.get)))
        } else {
          setup.context.log.debug("Cambio di direzione non necessario")
          Behaviors.same
        }

      } else {
        setup.context.log.warn("Ricevuto update model non valido")
        Behaviors.same
      }
    }
  }
}

