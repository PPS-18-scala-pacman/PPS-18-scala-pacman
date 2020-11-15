package it.unibo.scalapacman.server.core

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.scalapacman.common.{GameCharacter, UpdateModelDTO}
import it.unibo.scalapacman.lib.ai.GhostAI
import it.unibo.scalapacman.lib.model.Direction.Direction
import it.unibo.scalapacman.lib.model.{GameState, LevelState}
import it.unibo.scalapacman.server.core.Engine.{ChangeDirectionCur, ChangeDirectionReq}
import it.unibo.scalapacman.server.core.GhostAct.{Model, Setup}
import it.unibo.scalapacman.server.model.MoveDirection.directionToMoveDirection
import it.unibo.scalapacman.server.model.MoveDirection.MoveDirection

/**
 * Attore che rappresenta un fantasma durante il corso della partita è predisposto per ricevere le informazioni
 * sull’andamento del gioco e provvede, in base ad esse, all’elaborazione dei movimenti del fantasma che impersona
 */
object GhostAct {

  private case class Setup(gameId: String,
                           context: ActorContext[Engine.UpdateCommand],
                           engine: ActorRef[Engine.EngineCommand],
                           nickname: String,
                          )

  private case class Model(state: GameState, desMove: Option[MoveDirection])

  def apply(id: String, engine: ActorRef[Engine.EngineCommand], nickname: String): Behavior[Engine.UpdateCommand] =
    Behaviors.setup { context =>
      new GhostAct(Setup(id, context, engine, nickname: String)).coreRoutine(Model(GameState(0), None))
    }
}

private class GhostAct(setup: Setup) {

  setup.context.log.info(s"GhostAct avviato, fantasma: ${setup.nickname}")
  setup.engine ! Engine.RegisterWatcher(setup.context.self)

  /**
   * Behavior principale
   */
  private def coreRoutine(model: Model): Behaviors.Receive[Engine.UpdateCommand] =
    Behaviors.receiveMessage {
      case Engine.UpdateMsg(newModel) => handleEngineUpdate(newModel, model)
      case _ => Behaviors.same
    }

  /**
   * Elabora lo stato corrente della partita per definire le future mosse del fantasma
   *
   * @param model    modello di gioco attuale
   * @param myModel  modello dell'attore
   * @return         Behavior futuro
   */
  private def handleEngineUpdate(model: UpdateModelDTO, myModel: Model): Behavior[Engine.UpdateCommand] ={
    setup.context.log.debug("Ricevuto update: " + model)
    val gameState: GameState = model.state
    val selfDTO = model.gameEntities.find(_.id == setup.nickname)
    val pacmanList = model.gameEntities
      .filter(_.gameCharacterHolder.gameChar == GameCharacter.PACMAN)
      .map(_.toPacman.get)

    if(gameState.levelState != LevelState.ONGOING) {
      setup.context.log.info("Partita terminata spegnimento")
      Behaviors.stopped
    } else if(selfDTO.exists(_.isDead)) {
      setup.context.log.debug("Sono morto non posso muovermi")
      if (myModel.desMove.isDefined) setup.engine ! ChangeDirectionCur(setup.nickname)
      coreRoutine(Model(gameState, None))
    } else if (selfDTO.isDefined && pacmanList.nonEmpty) {
      setup.context.log.debug("Inizio calcolo percorso")

      val self = selfDTO.get.toGhost.get
      val pacman = GhostAI.choosePacmanToFollow(self, pacmanList)
      val direction: Option[Direction] = GhostAI.calculateDirectionClassic(self, pacman)
      //val direction = GhostAI.desiredDirection(selfDTO.get.toGhost.get, pacmanDTO.get.toPacman.get)(prologEngine, updatedMap)

      if (direction.isDefined) {
        if (!myModel.desMove.contains(direction.get)) setup.engine ! ChangeDirectionReq(setup.nickname, direction.get)
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

