package it.unibo.scalapacman.server.communication

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.scalapacman.server.communication.ServiceHandler.{ListingResponse, Setup, WrappedResponseCreateGame}
import it.unibo.scalapacman.server.core.{Game, Master}
import it.unibo.scalapacman.server.util.Settings

object ServiceHandler {

  private case class ListingResponse(listing: Receptionist.Listing) extends ServiceRoutes.RoutesCommand
  case class WrappedResponseCreateGame(response: Master.GameCreated) extends ServiceRoutes.RoutesCommand

  private case class Setup(context: ActorContext[ServiceRoutes.RoutesCommand])

  def apply(): Behavior[ServiceRoutes.RoutesCommand] =
    Behaviors.setup { context =>
      new ServiceHandler(Setup(context)).mainRoutine()
    }
}

private class ServiceHandler(setup: Setup) {

  val receptionistAdapter: ActorRef[Receptionist.Listing] = setup.context.messageAdapter[Receptionist.Listing](ListingResponse)
  val respondCreateGameAdapter: ActorRef[Master.GameCreated] = setup.context.messageAdapter(WrappedResponseCreateGame)

  def mainRoutine(): Behavior[ServiceRoutes.RoutesCommand] =
    Behaviors.receiveMessage {
      case ServiceRoutes.DeleteGame(gameId) =>
        val key = ServiceKey[Game.GameCommand](gameId)
        setup.context.system.receptionist ! Receptionist.Find(key, receptionistAdapter)
        deleteGameEx(key)
      case ServiceRoutes.CreateGame(replyTo) =>
        setup.context.system.receptionist ! Receptionist.Find(Master.masterServiceKey, receptionistAdapter)
        createGameEx(replyTo, Master.masterServiceKey)
    }

  def deleteGameEx(key: ServiceKey[Game.GameCommand]): Behavior[ServiceRoutes.RoutesCommand] =
    Behaviors.withStash(Settings.stashSize) { buffer =>
      Behaviors.receiveMessage {
        case ListingResponse(key.Listing(listings)) =>
          if(listings == null || listings.isEmpty) {
            setup.context.log.warn("Rilevata situazione anomala: nessun game da terminare trovato per key: " + key.id)
          } else {
            if(listings.size != 1) setup.context.log.warn("Rilevata situazione anomala: numero di game trovati maggiore di uno per key: " + key.id)
            listings.foreach(item => item ! Game.CloseCommand())
          }
          buffer.unstashAll(mainRoutine())
        case other: ServiceRoutes.RoutesCommand =>
          buffer.stash(other)
          Behaviors.same
      }
    }

  def createGameEx(replyTo: ActorRef[ServiceRoutes.Response], key: ServiceKey[Master.MasterCommand]): Behavior[ServiceRoutes.RoutesCommand] =
    Behaviors.withStash(Settings.stashSize) { buffer =>
      Behaviors.receiveMessage {
        case ListingResponse(key.Listing(listings)) =>
          if(listings.size < 1) {
            replyTo ! ServiceRoutes.Failure("Errore, servizio di gioco non attivo")
            buffer.unstashAll(mainRoutine())
          } else {
            listings.head ! Master.CreateGame(respondCreateGameAdapter)
            Behaviors.same
          }
        case WrappedResponseCreateGame(response) =>
          if(replyTo != null) replyTo ! ServiceRoutes.Success(response.gameId)
          buffer.unstashAll(mainRoutine())
        case other: ServiceRoutes.RoutesCommand =>
          buffer.stash(other)
          Behaviors.same
      }
    }
}
