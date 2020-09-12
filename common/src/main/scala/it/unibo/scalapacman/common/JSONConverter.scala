package it.unibo.scalapacman.common

import java.io.StringWriter

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.{JsonMappingException, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import grizzled.slf4j.Logging

import scala.reflect.{ClassTag, classTag}

/**
 * Contiene le funzioni di utility per la conversione di un oggetto in JSON e viceversa
 */
object JSONConverter extends Logging {
  private val CONVERSION_ERROR: String = "Conversione comando fallita"

  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  /**
   * Converte un oggetto in un JSON
   */
  def toJSON(value: Object): String = {
    val out = new StringWriter()
    mapper.writeValue(out, value)
    out.toString
  }

  /**
   * Converte un JSON in un oggetto
   *
   * @param jsonMsg la stringa JSON da convertire
   * @tparam A      la classe in cui convertire il messaggio
   * @return        il messaggio convertito, rappresentato come Option
   */
  def fromJSON[A: ClassTag](jsonMsg: String): Option[A] = {
    try {
      Some(mapper.readerFor(classTag[A].runtimeClass.asInstanceOf[Class[A]]).readValue(jsonMsg))
    } catch {
      case ex @ ( _ :JsonProcessingException | _ :JsonMappingException) =>
        error(s"$CONVERSION_ERROR: $ex.getMessage")
        None
    }
  }

}
