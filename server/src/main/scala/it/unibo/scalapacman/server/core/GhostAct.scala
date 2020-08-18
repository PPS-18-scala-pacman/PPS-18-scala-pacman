package it.unibo.scalapacman.server.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.scalapacman.common.{GameCharacter, UpdateModelDTO}
import it.unibo.scalapacman.lib.Utility.directionByCrossTile
import it.unibo.scalapacman.lib.ai.GhostAI
import it.unibo.scalapacman.lib.ai.GhostAI.prologEngine
import it.unibo.scalapacman.lib.engine.GameHelpers.{CharacterHelper, MapHelper}
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.GhostType.GhostType
import it.unibo.scalapacman.lib.model.{Character, Direction, GameState, Ghost, Map}
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

  setup.context.log.info(s"GhostActor per fantasma ${setup.ghostType}")
  setup.engine ! Engine.RegisterGhost(setup.context.self, setup.ghostType)

  private def coreRoutine(model: Model): Behaviors.Receive[Engine.UpdateCommand] =
    Behaviors.receiveMessage {
      case Engine.UpdateMsg(newModel) => handleEngineUpdate(newModel, model)
      case _ => Behaviors.same
    }

  private def handleEngineUpdate(model: UpdateModelDTO, myModel: Model): Behavior[Engine.UpdateCommand] ={
    setup.context.log.debug("Ricevuto update: " + model)

    val selfDTO = model.gameEntities.find(_.gameCharacterHolder.gameChar == GameCharacter.ghostTypeToGameCharacter(setup.ghostType))
    val pacmanDTO = model.gameEntities.find(_.gameCharacterHolder.gameChar == GameCharacter.PACMAN)
    val gameState: GameState = model.state

    //FIXME UPDATE DELLA MAPPA
    implicit val updatedMap: Map = myModel.map

    if(selfDTO.isDefined && pacmanDTO.isDefined) {

      val pacman = pacmanDTO.get.toPacman.get
      val direction: Option[Direction] = calculateDirection(selfDTO.get.toGhost.get, pacman)
      //val direction = GhostAI.desiredDirection(selfDTO.get.toGhost.get, pacmanDTO.get.toPacman.get)(prologEngine, updatedMap)

      if (direction.isDefined) {
        val move: MoveDirection = direction.get match {
          case Direction.NORTH |
               Direction.NORTHEAST |
               Direction.NORTHWEST => MoveDirection.UP
          case Direction.SOUTH |
               Direction.SOUTHEAST |
               Direction.SOUTHWEST => MoveDirection.DOWN
          case Direction.EAST => MoveDirection.RIGHT
          case Direction.WEST => MoveDirection.LEFT
        }

        if (!myModel.desMove.contains(move)) setup.engine ! ChangeDirectionReq(setup.context.self, move)

        coreRoutine(Model(updatedMap, gameState, Some(move)))
      } else {
        setup.context.log.debug("Cambio di direzione non necessario")
        Behaviors.same
      }

    } else {
      setup.context.log.warn("Ricevuto update model non valido")
      Behaviors.same
    }
  }

  private def calculateDirection(self: Ghost, char: Character)(implicit map: Map):Option[Direction] = {
    val endTileIndexes = char.nextCrossTile()

    if(endTileIndexes.isDefined) {
      if(self.tileIsCross) {
        val nearCross = map.tileNearbyCrossings(endTileIndexes.get, char)
        if(nearCross.size == 2 && nearCross.contains(self.tileIndexes) ) {
          directionByCrossTile(self.tileIndexes :: nearCross.filter(_!=self.tileIndexes), char)
        } else {
          GhostAI.desiredDirectionClassic(self, endTileIndexes.get)
        }
      } else {
        self.directionForTurn
      }
    } else {
      None
    }
  }

}

