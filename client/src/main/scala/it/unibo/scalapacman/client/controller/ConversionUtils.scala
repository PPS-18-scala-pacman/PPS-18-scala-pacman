package it.unibo.scalapacman.client.controller

import java.io.StringWriter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import it.unibo.scalapacman.common.{Command, MoveCommandTypeHolder}

object ConversionUtils {

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def convertMoveCommandTypeHolder(moveCommandTypeHolder: MoveCommandTypeHolder): String = convertToJSON(moveCommandTypeHolder)

  def convertCommand(command: Command): String = convertToJSON(command)

  private def convertToJSON(value: Object): String = {
    val out = new StringWriter()
    mapper.writeValue(out, value)
    out.toString
  }
}
