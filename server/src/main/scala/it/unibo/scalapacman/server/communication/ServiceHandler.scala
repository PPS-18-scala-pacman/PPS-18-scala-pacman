package it.unibo.scalapacman.server.communication

import akka.NotUsed

import scala.util.{Failure, Success}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.{Flow, Source}
import it.unibo.scalapacman.server.communication.ConnectionProtocol.ConnectionMsg
import it.unibo.scalapacman.server.communication.ServiceHandler.{ListingResponse, PlayerRegisterRespFailure}
import it.unibo.scalapacman.server.communication.ServiceHandler.{PlayerRegisterRespSuccess, Setup, WrapRespCreateGame}
import it.unibo.scalapacman.server.communication.StreamFactory.{createActorWBSink, createActorWBSource}
import it.unibo.scalapacman.server.core.Player.{PlayerRegistration, RegistrationAccepted, RegistrationRejected}
import it.unibo.scalapacman.server.core.{Game, Master}
import it.unibo.scalapacman.server.config.Settings
import it.unibo.scalapacman.server.config.Settings.askTimeout

object ServiceHandler {

  private case class ListingResponse(listing: Receptionist.Listing) extends ServiceRoutes.RoutesCommand
  case class WrapRespCreateGame(response: Master.GameCreated) extends ServiceRoutes.RoutesCommand
  case class PlayerRegisterRespSuccess(messageHandler: ActorRef[ConnectionMsg], source: Source[Message, NotUsed]) extends ServiceRoutes.RoutesCommand
  case class PlayerRegisterRespFailure(cause: String) extends ServiceRoutes.RoutesCommand

  private case class Setup(context: ActorContext[ServiceRoutes.RoutesCommand])

  def apply(): Behavior[ServiceRoutes.RoutesCommand] =
    Behaviors.setup { context =>
      new ServiceHandler(Setup(context)).mainRoutine()
    }
}

private class ServiceHandler(setup: Setup) {

  implicit val system: ActorSystem[Nothing] = setup.context.system
  val receptionistAdapter: ActorRef[Receptionist.Listing] = setup.context.messageAdapter[Receptionist.Listing](ListingResponse)
  val respondCreateGameAdapter: ActorRef[Master.GameCreated] = setup.context.messageAdapter(WrapRespCreateGame)

  def mainRoutine(): Behavior[ServiceRoutes.RoutesCommand] =
    Behaviors.receiveMessage {
      case ServiceRoutes.DeleteGame(gameId) =>
        val key = ServiceKey[Game.GameCommand](gameId)
        setup.context.system.receptionist ! Receptionist.Find(key, receptionistAdapter)
        deleteGameEx(key)
      case ServiceRoutes.CreateGame(replyTo) =>
        setup.context.system.receptionist ! Receptionist.Find(Master.masterServiceKey, receptionistAdapter)
        createGameEx(replyTo, Master.masterServiceKey)
      case ServiceRoutes.CreateConnectionGame(replyTo, gameId) =>
        val key = ServiceKey[Game.GameCommand](gameId)
        setup.context.system.receptionist ! Receptionist.Find(key, receptionistAdapter)
        craeteGameConnectionEx(replyTo, key)
    }

  def deleteGameEx(key: ServiceKey[Game.GameCommand]): Behavior[ServiceRoutes.RoutesCommand] =
    Behaviors.withStash(Settings.stashSize) { buffer =>
      Behaviors.receiveMessage {
        case ListingResponse(key.Listing(listings)) =>
          if(listings == null || listings.isEmpty) {
            setup.context.log.warn(s"Rilevata situazione anomala: nessun game da terminare trovato per key: ${key.id}") //scalastyle:ignore
          } else {
            if(listings.size != 1) setup.context.log.warn(s"Rilevata situazione anomala: numero di game trovati maggiore di uno per key: ${key.id}")
            listings.foreach(item => item ! Game.CloseCommand())
          }
          buffer.unstashAll(mainRoutine())
        case other: ServiceRoutes.RoutesCommand =>
          buffer.stash(other)
          Behaviors.same
      }
    }

  def createGameEx(replyTo: ActorRef[ServiceRoutes.ResponseCreateGame],
                   key: ServiceKey[Master.MasterCommand]): Behavior[ServiceRoutes.RoutesCommand] =
    Behaviors.withStash(Settings.stashSize) { buffer =>
      Behaviors.receiveMessage {
        case ListingResponse(key.Listing(listings)) =>
          if(listings.size < 1) {
            replyTo ! ServiceRoutes.FailureCrG("Errore, servizio di gioco non attivo")
            buffer.unstashAll(mainRoutine())
          } else {
            listings.head ! Master.CreateGame(respondCreateGameAdapter)
            Behaviors.same
          }
        case WrapRespCreateGame(response) =>
          if(replyTo != null) replyTo ! ServiceRoutes.SuccessCrG(response.gameId)
          buffer.unstashAll(mainRoutine())
        case other: ServiceRoutes.RoutesCommand =>
          buffer.stash(other)
          Behaviors.same
      }
    }

  def craeteGameConnectionEx(replyTo: ActorRef[ServiceRoutes.ResponseConnGame],
                             key: ServiceKey[Game.GameCommand]): Behavior[ServiceRoutes.RoutesCommand] =
    Behaviors.withStash(Settings.stashSize) { buffer =>

      setup.context.log.info(s"Richiesta connessione per game ${key.id}")

      def safeTell[T](replyTo: ActorRef[T], msg: T): Unit = {
        if(replyTo != null) replyTo ! msg
      }

      def handleDiscoveryError(errMsg: String): Behavior[ServiceRoutes.RoutesCommand] = {
        setup.context.log.error(errMsg)
        safeTell(replyTo, ServiceRoutes.FailureConG(errMsg))
        buffer.unstashAll(mainRoutine())
      }

      def sendRegisterRequest(gameAct: ActorRef[Game.GameCommand]): Behavior[ServiceRoutes.RoutesCommand] = {
        val (actorSourceRef, source) = createActorWBSource().preMaterialize()
        source.run()
        setup.context.ask[Game.GameCommand, PlayerRegistration](gameAct, Game.RegisterPlayer(_, actorSourceRef)) {
          case Success(RegistrationAccepted(messageHandler)) => PlayerRegisterRespSuccess(messageHandler, source)
          case Success(RegistrationRejected(errMsg)) => PlayerRegisterRespFailure(errMsg)
          case Failure(errMsg) => PlayerRegisterRespFailure(errMsg.getMessage)
        }
        Behaviors.same
      }

      Behaviors.receiveMessage {
        case ListingResponse(key.Listing(listings)) if listings == null || listings.isEmpty =>
          handleDiscoveryError(s"Rilevata situazione anomala: nessun game da terminare trovato per key: ${key.id}")
        case ListingResponse(key.Listing(listings)) if listings.size != 1 =>
          handleDiscoveryError(s"Rilevata situazione anomala: numero di game trovati maggiore di uno per key: ${key.id}")
        case ListingResponse(key.Listing(listings)) =>
          sendRegisterRequest(listings.head)
        case PlayerRegisterRespSuccess(messageHandler, source) =>
          val flow = Flow.fromSinkAndSourceCoupled(createActorWBSink(messageHandler), source)
          safeTell(replyTo, ServiceRoutes.SuccessConG(flow))
          buffer.unstashAll(mainRoutine())
        case PlayerRegisterRespFailure(errMsg) =>
          safeTell(replyTo, ServiceRoutes.FailureConG(errMsg))
          buffer.unstashAll(mainRoutine())
        case other: ServiceRoutes.RoutesCommand =>
          buffer.stash(other)
          Behaviors.same
      }
    }
}
