package it.unibo.scalapacman.server.communication

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior, PostStop}
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import it.unibo.scalapacman.server.communication.HttpService.{Message, Setup, StartFailed, Started, Stop}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object HttpService {

  sealed trait Message
  case class StartFailed(cause: Throwable) extends Message
  case class Started(binding: ServerBinding) extends Message
  case class Stop() extends Message

  private case class Setup(context: ActorContext[Message], host: String, port: Int)

  private def startHttpServer(routes: Route, system: ActorSystem[_], host: String, port: Int): Future[Http.ServerBinding] = {
    // Akka HTTP lavora ancora con il classic ActorSystem
    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic

    Http().bindAndHandle(routes, host, port)
  }

  def apply(host: String, port: Int): Behavior[Message] =
    Behaviors.setup { context =>

      context.log.info("HttpService avvio")
      implicit val system: ActorSystem[Nothing] = context.system

      val routesHandler = context.spawn(ServiceHandler(), "RoutesHandler")
      val routes = ServiceRoutes(routesHandler)

      val serverBinding: Future[Http.ServerBinding] = startHttpServer(routes, context.system, host, port)
      context.pipeToSelf(serverBinding) {
        case Success(binding) => Started(binding)
        case Failure(ex) => StartFailed(ex)
      }

      new HttpService(Setup(context, host, port)).starting(false)
    }
}

private class HttpService(setup: Setup) {

  def running(binding: ServerBinding): Behavior[Message] =
    Behaviors.receiveMessagePartial[Message] {
      case Stop() =>
        setup.context.log.info(
          "Stopping server http://{}:{}/",
          binding.localAddress.getHostString,
          binding.localAddress.getPort)
        Behaviors.stopped
    }.receiveSignal {
      case (_, PostStop) =>
        binding.unbind()
        Behaviors.same
    }

  def starting(wasStopped: Boolean): Behaviors.Receive[Message] =
    Behaviors.receiveMessage[Message] {
      case StartFailed(cause) =>
        throw new RuntimeException("Server failed to start", cause)
      case Started(binding) =>
        setup.context.log.info(
          "Server online at http://{}:{}/",
          binding.localAddress.getHostString,
          binding.localAddress.getPort)
        if(wasStopped) setup.context.self ! Stop()
        running(binding)
      case Stop() =>
        // il server non può essere arrestato finchè l'avvio non è stato ultimato
        starting(true)
    }
}
