package it.unibo.scalapacman.server.util

import java.io.StringWriter

import akka.actor.typed.ActorRef
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.{JsonMappingException, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import grizzled.slf4j.Logging
import it.unibo.scalapacman.common.{Command, CommandType, CommandTypeHolder, MoveCommandType, MoveCommandTypeHolder, UpdateModel}
import it.unibo.scalapacman.server.core.Engine
import it.unibo.scalapacman.server.core.Engine.UpdateCommand

object ConversionUtils extends Logging{

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def convertModel(model: UpdateModel): String = {
    val out = new StringWriter
    mapper.writeValue(out, model)
    out.toString
  }

  def convertClientMsg(jsonMsg: String, actRef: ActorRef[UpdateCommand]): Option[Engine.GameEntityCommand] = {
    try {
      mapper.readValue(jsonMsg, classOf[Command]) match {
        case Command(CommandTypeHolder(CommandType.PAUSE), None) => Some(Engine.SwitchGameState())
        case Command(CommandTypeHolder(CommandType.MOVE), Some(data)) => convertMoveDataMsg(data, actRef)
        case _ => error("Comando non riconosciuto"); None
      }
    } catch {
      case ex @ ( _ :JsonProcessingException | _ :JsonMappingException) =>
        error("Conversione Comando fallita: " + ex.getMessage)
        None
    }
  }

  private def convertMoveDataMsg(data: String, actRef: ActorRef[UpdateCommand]): Option[Engine.GameEntityCommand] = {
    mapper.readValue(data, classOf[MoveCommandTypeHolder]) match {
      case MoveCommandTypeHolder(MoveCommandType.UP) =>
        Some(Engine.ChangeDirectionReq(actRef, Engine.MoveDirection.UP))
      case MoveCommandTypeHolder(MoveCommandType.DOWN) =>
        Some(Engine.ChangeDirectionReq(actRef, Engine.MoveDirection.DOWN))
      case MoveCommandTypeHolder(MoveCommandType.LEFT) =>
        Some(Engine.ChangeDirectionReq(actRef, Engine.MoveDirection.LEFT))
      case MoveCommandTypeHolder(MoveCommandType.RIGHT) =>
        Some(Engine.ChangeDirectionReq(actRef, Engine.MoveDirection.RIGHT))
      case MoveCommandTypeHolder(MoveCommandType.NONE) =>
        Some(Engine.ChangeDirectionCur(actRef))
      case _ => error("Comando di movimento non riconosciuto"); None
    }
  }
}
