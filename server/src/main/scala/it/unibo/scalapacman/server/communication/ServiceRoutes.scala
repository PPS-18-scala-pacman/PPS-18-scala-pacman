package it.unibo.scalapacman.server.communication

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._ // scalastyle:ignore underscore.import
import akka.http.scaladsl.server.Route
import it.unibo.scalapacman.server.util.Settings.askTimeout

import scala.concurrent.Future

object ServiceRoutes {

  trait RoutesCommand
  case class DeleteGame(gameId: String) extends RoutesCommand
  case class CreateGame(replyTo: ActorRef[Response]) extends RoutesCommand

  sealed trait Response
  case class OK(gameId: String) extends Response
  case class KO(reason: String) extends Response

  private case class ListingResponse(listing: Receptionist.Listing)

  def apply(handler: ActorRef[RoutesCommand])(implicit system: ActorSystem[_]): Route =
    pathPrefix("games") {
      concat (
        pathEnd {
          post {
            val operationPerformed: Future[Response] = handler.ask(CreateGame)
            onSuccess(operationPerformed) {
              case ServiceRoutes.OK(gameId) => complete(StatusCodes.Created, gameId)
              case ServiceRoutes.KO(reason) => complete(StatusCodes.InternalServerError -> reason)
            }
          }
        },
        pathPrefix(Segment) { gameId =>
          pathEnd {
            delete {
              handler ! DeleteGame(gameId)
              complete((StatusCodes.Accepted, "delete request received"))
            }
          }
        }
      )
    }

}
