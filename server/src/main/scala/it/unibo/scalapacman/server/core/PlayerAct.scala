package it.unibo.scalapacman.server.core

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import it.unibo.scalapacman.common.{Command, CommandType, CommandTypeHolder, JSONConverter, MoveCommandType, MoveCommandTypeHolder}
import it.unibo.scalapacman.server.communication.ConnectionProtocol.{ConnectionAck, ConnectionData, ConnectionEnded,
  ConnectionFailed, ConnectionInit, ConnectionMsg}
import it.unibo.scalapacman.server.core.Engine.EngineCommand
import it.unibo.scalapacman.server.core.Game.GameCommand
import it.unibo.scalapacman.server.core.PlayerAct.{PlayerCommand, RegisterUser, RegistrationAccepted,
  RegistrationRejected, Setup, WrapRespMessage, WrapRespUpdate}
import it.unibo.scalapacman.server.model.MoveDirection

/**
 * Attore che rappresenta un giocatore, scambia informazioni col Client, in modo tale da comunicare al sistema
 * i comandi ricevuti durante la sessione di gioco e di inviare lo stato aggiornato della partita
 */
object PlayerAct {

  // Messaggi gestiti dall'attore
  sealed trait PlayerCommand
  case class RegisterUser(replyTo: ActorRef[PlayerRegistration], sourceAct: ActorRef[Message], nickname: String) extends PlayerCommand

  case class WrapRespMessage(response: ConnectionMsg) extends PlayerCommand
  case class WrapRespUpdate(response: Engine.UpdateCommand) extends PlayerCommand

  // Messaggi di notifica registrazione
  sealed trait PlayerRegistration
  case class RegistrationAccepted(messageHandler: ActorRef[ConnectionMsg]) extends PlayerRegistration
  case class RegistrationRejected(cause: String) extends PlayerRegistration

  private case class Setup(gameId: String,
                           context: ActorContext[PlayerCommand],
                           engine: ActorRef[Engine.EngineCommand],
                           game: ActorRef[Game.GameCommand])

  def apply(gameId: String, engine: ActorRef[Engine.EngineCommand], game: ActorRef[Game.GameCommand]): Behavior[PlayerCommand] =
    Behaviors.setup { context =>
      new PlayerAct(Setup(gameId, context, engine, game)).initRoutine()
    }
}

class PlayerAct(setup: Setup) {

  val clientMsgAdapter: ActorRef[ConnectionMsg] = setup.context.messageAdapter(WrapRespMessage)
  val updateMsgAdapter: ActorRef[Engine.UpdateCommand] = setup.context.messageAdapter(WrapRespUpdate)

  /**
   * Behavior iniziale di attesa richiesta connessione
   */
  private def initRoutine(): Behavior[PlayerCommand] =
    Behaviors.receiveMessage {
      case RegisterUser(replyTo, sourceAct, nickname) =>
        setup.engine ! Engine.RegisterWatcher(updateMsgAdapter)
        replyTo ! RegistrationAccepted(clientMsgAdapter)
        setUpConnectionRoutine(sourceAct, nickname)
      case _ =>
        setup.context.log.warn("Ricevuto messaggio non gestito")
        Behaviors.same
    }

  /**
   * Behavior di attesa instauramento connessione
   */
  private def setUpConnectionRoutine(sourceAct: ActorRef[Message], nickname: String): Behavior[PlayerCommand] =
    Behaviors.receiveMessage {
      case RegisterUser(replyTo, _, _) =>
        replyTo ! RegistrationRejected("Player occupato")
        Behaviors.same
      case WrapRespMessage(ConnectionInit(act)) =>
        setup.context.log.info("Ricevuto messaggio connessione instaurata")
        act ! ConnectionAck()
        setup.game ! Game.NotifyPlayerReady(nickname)
        mainRoutine(sourceAct, nickname)
      case WrapRespMessage(ConnectionFailed(ex)) =>
        setup.context.log.error("Ricevuto messaggio connessione fallita: " + ex.getMessage)
        setup.context.log.debug(ex.getStackTrace.toString)
        throw ex
      case _ =>
        setup.context.log.warn("Ricevuto messaggio non gestito")
        Behaviors.same
    }

  /**
   * Behavior principale usato durante il corso della sessione di gioco
   */
  private def mainRoutine(sourceAct: ActorRef[Message], nickname: String): Behavior[PlayerCommand] =
    Behaviors.receiveMessage {
      case RegisterUser(replyTo, _, _) =>
        replyTo ! RegistrationRejected("Player occupato")
        Behaviors.same
      case WrapRespUpdate(Engine.UpdateMsg(model)) =>
        setup.context.log.debug("Ricevuto update: " + model)
        val msg = JSONConverter.toJSON(model)
        sourceAct ! TextMessage(msg)
        Behaviors.same
      case WrapRespMessage(ConnectionData(act, TextMessage.Strict(msg))) =>
        setup.context.log.debug("Ricevuto messaggio: " + msg)
        val command = JSONConverter.fromJSON[Command](msg) flatMap (parseClientCommand(_, nickname))
        if(command.isDefined) sendCommand(command.get)
        act ! ConnectionAck()
        if(command.isDefined && command.get.isInstanceOf[Game.PlayerLeftGame]) {
          setup.context.log.info(s"Il Player $nickname ha abbandonato la partita")
          setup.engine ! Engine.UnRegisterWatcher(updateMsgAdapter)
          Behaviors.stopped
        } else {
          Behaviors.same
        }
      case WrapRespMessage(ConnectionEnded()) =>
        setup.context.log.error("Ricevuto messaggio connessione chiusa")
        setup.engine ! Engine.UnRegisterWatcher(updateMsgAdapter)
        throw new IllegalStateException("Connessione chiusa in modo anomalo")
      case WrapRespMessage(ConnectionFailed(ex)) =>
        setup.context.log.error(s"Ricevuto messaggio connessione fallita ${ex.getMessage}")
        setup.context.log.debug(ex.getStackTrace.toString)
        setup.engine ! Engine.UnRegisterWatcher(updateMsgAdapter)
        throw ex
      case msg:Any =>
        setup.context.log.warn(s"Ricevuto messaggio non gestito: $msg")
        Behaviors.same
    }

  /**
   * Parsing Comando del client
   *
   * @param clientCommand comando da parsare
   * @param nickname      self
   * @return              EngineCommand corrispondente
   */
  private def parseClientCommand(clientCommand: Command, nickname: String): Option[Any] = clientCommand match {
    case Command(CommandTypeHolder(CommandType.PAUSE), None) => Some(Engine.Pause())
    case Command(CommandTypeHolder(CommandType.RESUME), None) => Some(Engine.Resume())
    case Command(CommandTypeHolder(CommandType.LEFT_GAME), None) => Some(Game.PlayerLeftGame(nickname))
    case Command(CommandTypeHolder(CommandType.MOVE), Some(data)) =>
      JSONConverter.fromJSON[MoveCommandTypeHolder](data) flatMap (parseClientMoveCommand(_, nickname))
    case _ => None
  }

  /**
   * Parsing di un Comando di movimento
   *
   * @param clientMoveCommand comando di movimento da parsare
   * @param nickname          self
   * @return                  EngineCommand corrispondente
   */
  private def parseClientMoveCommand(clientMoveCommand: MoveCommandTypeHolder, nickname: String): Option[Engine.EngineCommand] =
    clientMoveCommand match {
      case MoveCommandTypeHolder(MoveCommandType.UP) =>
        Some(Engine.ChangeDirectionReq(nickname, MoveDirection.UP))
      case MoveCommandTypeHolder(MoveCommandType.DOWN) =>
        Some(Engine.ChangeDirectionReq(nickname, MoveDirection.DOWN))
      case MoveCommandTypeHolder(MoveCommandType.LEFT) =>
        Some(Engine.ChangeDirectionReq(nickname, MoveDirection.LEFT))
      case MoveCommandTypeHolder(MoveCommandType.RIGHT) =>
        Some(Engine.ChangeDirectionReq(nickname, MoveDirection.RIGHT))
      case MoveCommandTypeHolder(MoveCommandType.NONE) =>
        Some(Engine.ChangeDirectionCur(nickname))
      case _ => None
    }

  private def sendCommand(command: Any): Unit = command match {
    case cmd: EngineCommand => setup.engine ! cmd
    case cmd: GameCommand => setup.game ! cmd
    case cmd: Any => setup.context.log.error("Impossibile inviare messaggio, comando non valido: " + cmd)
  }
}
