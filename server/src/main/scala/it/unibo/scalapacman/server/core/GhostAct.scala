package it.unibo.scalapacman.server.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.scalapacman.common.{GameCharacter, UpdateModelDTO}
import it.unibo.scalapacman.lib.ai.GhostAI
import it.unibo.scalapacman.lib.ai.GhostAI.prologEngine
import it.unibo.scalapacman.lib.engine.GameHelpers.CharacterHelper
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.lib.model.Map.MapIndexes
import it.unibo.scalapacman.lib.model.{Direction, GameState, Ghost, Map}
import it.unibo.scalapacman.server.core.Engine.ChangeDirectionReq
import it.unibo.scalapacman.server.core.GhostAct.{Model, Setup}
import it.unibo.scalapacman.server.model.MoveDirection
import it.unibo.scalapacman.server.model.MoveDirection.MoveDirection

object GhostAct {

  private case class Setup(gameId: String,
                           context: ActorContext[Engine.UpdateCommand],
                           engine: ActorRef[Engine.EngineCommand],
                           ghostType: GhostType)

  case class Model(map: Map, state: GameState, desMove: Option[MoveDirection])

  def apply(id: String, engine: ActorRef[Engine.EngineCommand], ghostType: GhostType): Behavior[Engine.UpdateCommand] =
    Behaviors.setup { context =>
      new GhostAct(Setup(id, context, engine, ghostType)).coreRoutine(Model(Map.classic, GameState(0), None))
    }
}


private class GhostAct(setup: Setup) {

  setup.engine ! Engine.RegisterGhost(setup.context.self, setup.ghostType)

  private def coreRoutine(model: Model): Behaviors.Receive[Engine.UpdateCommand] =
    Behaviors.receiveMessage {
      case Engine.UpdateMsg(newModel) => handleEngineUpdate(newModel, model)
      case _ => Behaviors.same
    }

  private def handleEngineUpdate(model: UpdateModelDTO, myModel: Model): Behavior[Engine.UpdateCommand] ={
    setup.context.log.info("Ricevuto update: " + model)

    val selfDTO = model.gameEntities.find(_.gameCharacterHolder.gameChar == GameCharacter.ghostTypeToGameCharacter(setup.ghostType))
    val pacmanDTO = model.gameEntities.find(_.gameCharacterHolder.gameChar == GameCharacter.PACMAN)
    val gameState: GameState = model.state

    //FIXME UPDATE DELLA MAPPA
    implicit val updatedMap: Map = myModel.map
    val pacmanNextMove = pacmanDTO.get.toPacman.get.nextCrossTile

    if(selfDTO.isDefined && pacmanDTO.isDefined && pacmanNextMove.isDefined) {

      val direction: Option[Direction] = calculateDirection(selfDTO.get.toGhost.get, pacmanNextMove.get)
      //val direction = GhostAI.desiredDirection(self.get.toGhost.get, pacman.get.toPacman.get)(prologEngine, updatedMap)

      val move: MoveDirection = direction match {
        case Direction.NORTH |
             Direction.NORTHEAST |
             Direction.NORTHWEST => MoveDirection.UP
        case Direction.SOUTH |
             Direction.SOUTHEAST |
             Direction.SOUTHWEST => MoveDirection.DOWN
        case Direction.EAST => MoveDirection.RIGHT
        case Direction.WEST => MoveDirection.LEFT
      }

      if(!myModel.desMove.contains(move)) setup.engine ! ChangeDirectionReq(setup.context.self, move)

      coreRoutine(Model(updatedMap, gameState, Some(move)))
    } else {
      setup.context.log.warn("Ricevuto update model non valido")
      Behaviors.same
    }
  }

  private def calculateDirection(self: Ghost, endTileIndexes: MapIndexes)(implicit map: Map):Option[Direction] = {
    if(self.isCross) {
      GhostAI.desiredDirectionClassic(self, endTileIndexes)
    } else {
      self.directionForTurn
      //TODO in caso None potrei già calcolare la nuova dir per dove si trova pacman per portarmi avanti e introdurre
      // controlli per non ricolcolare nulla nei cicli successivi se pacman ed io non cambiamo nextTileCross
      // (nel caso togliere Option dal return)
    }
  }

}

