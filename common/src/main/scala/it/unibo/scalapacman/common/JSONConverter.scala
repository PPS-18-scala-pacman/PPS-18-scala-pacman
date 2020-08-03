package it.unibo.scalapacman.common

import java.io.StringWriter

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.{JsonMappingException, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import grizzled.slf4j.Logging

import scala.reflect.{ClassTag, classTag}

object JSONConverter extends Logging {
  val UNKNOWN_COMMAND: String = "Comando non riconosciuto"
  val CONVERSION_ERROR: String = "Conversione comando fallita"

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def toJSON(value: Object): String = {
    val out = new StringWriter()
    mapper.writeValue(out, value)
    out.toString
  }

  def fromJSON[A: ClassTag](jsonMsg: String): Option[A] = {
    try {
      mapper.readValue(jsonMsg, classTag[A].runtimeClass.asInstanceOf[Class[A]]) match {
        case valueRead: A => Some(valueRead)
        case _ => error(UNKNOWN_COMMAND); None
      }
    } catch {
      case ex @ ( _ :JsonProcessingException | _ :JsonMappingException) =>
        error(s"$CONVERSION_ERROR: $ex.getMessage")
        None
    }
  }

}
