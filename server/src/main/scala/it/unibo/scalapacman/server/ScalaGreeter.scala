package it.unibo.scalapacman.server

trait ScalaGreeter {
  def sayHello: String
}

object ScalaGreeter {
  def apply(): ScalaGreeter = new ScalaGreeterImpl
}

class ScalaGreeterImpl extends ScalaGreeter {
  override def sayHello: String = "Hello, World!"
}
