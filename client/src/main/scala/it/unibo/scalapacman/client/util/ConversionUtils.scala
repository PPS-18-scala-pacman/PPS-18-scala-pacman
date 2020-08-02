package it.unibo.scalapacman.client.util

import java.io.StringWriter

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.{JsonMappingException, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import grizzled.slf4j.Logging
import it.unibo.scalapacman.common.{Command, MoveCommandTypeHolder, UpdateModelDTO}

object ConversionUtils extends Logging {
  val UNKNOWN_COMMAND: String = "Comando non riconosciuto"
  val CONVERSION_ERROR: String = "Conversione comando fallita"

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def convertMoveCommandTypeHolder(moveCommandTypeHolder: MoveCommandTypeHolder): String = convertToJSON(moveCommandTypeHolder)

  def convertCommand(command: Command): String = convertToJSON(command)

  def convertServerMsg(jsonMsg: String): Option[UpdateModelDTO] = {
    try {
      mapper.readValue(jsonMsg, classOf[UpdateModelDTO]) match {
        case updateModel@UpdateModelDTO(_, _, _, _) => Some(updateModel)
        case _ => error(UNKNOWN_COMMAND); None
      }
    } catch {
      case ex @ ( _ :JsonProcessingException | _ :JsonMappingException) =>
        error(s"$CONVERSION_ERROR: $ex.getMessage")
        None
    }
  }

  private def convertToJSON(value: Object): String = {
    val out = new StringWriter()
    mapper.writeValue(out, value)
    out.toString
  }
}
