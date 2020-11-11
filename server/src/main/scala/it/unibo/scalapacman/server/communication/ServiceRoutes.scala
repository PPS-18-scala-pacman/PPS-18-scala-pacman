package it.unibo.scalapacman.server.communication

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._ // scalastyle:ignore
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import it.unibo.scalapacman.server.config.Settings
import it.unibo.scalapacman.server.config.Settings.askTimeout
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._ // scalastyle:ignore

import scala.concurrent.Future

/**
 * Elemento core del server HTTP definisce l'insieme dei servizi esposti inoltre si occupa smistare le
 * richieste in arrivo all'handler
 */
object ServiceRoutes {

  // Messaggi di gestione richieste ricevute
  trait RoutesCommand
  case class DeleteGame(gameId: String) extends RoutesCommand
  case class CreateGame(replyTo: ActorRef[ResponseCreateGame], playerNumber: Int) extends RoutesCommand
  case class CreateConnectionGame(replyTo: ActorRef[ResponseConnGame], gameId: String, nickname: String) extends RoutesCommand

  // Messaggi di risposta per creazione nuova partita
  sealed trait ResponseCreateGame
  case class SuccessCrG(gameId: String) extends ResponseCreateGame
  case class FailureCrG(reason: String) extends ResponseCreateGame

  // Messaggi di risposta per richiesta nuova connessione
  sealed trait ResponseConnGame
  case class SuccessConG(flow: Flow[Message, Message, Any]) extends ResponseConnGame
  case class FailureConG(reason: String) extends ResponseConnGame

  case class CreateGameRequest(playersNumber: Int)
  object JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val createGameRequestFormat: RootJsonFormat[CreateGameRequest] = jsonFormat1(CreateGameRequest)
  }

  private case class ListingResponse(listing: Receptionist.Listing)

  import ServiceRoutes.JsonSupport._ // scalastyle:ignore

  def apply(handler: ActorRef[RoutesCommand])(implicit system: ActorSystem[_]): Route =
    concat(
      pathPrefix("games") {
        concat (
          pathEnd {
            post {
              entity(as[CreateGameRequest]) { req =>
                if(req.playersNumber < 1 || req.playersNumber > Settings.maxPlayersNumber) {
                  complete(StatusCodes.UnprocessableEntity -> "Numero di giocatori non valido")
                } else {
                  val operationPerformed: Future[ResponseCreateGame] = handler.ask(CreateGame(_, req.playersNumber))
                  onSuccess(operationPerformed) {
                    case ServiceRoutes.SuccessCrG(gameId) => complete(StatusCodes.Created, gameId)
                    case ServiceRoutes.FailureCrG(reason) => complete(StatusCodes.InternalServerError -> reason)
                  }
                }
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
      },
      path("connection-management" / "games" / Segment) { gameId: String =>
        parameters('playerName.as[String]) { nickname =>
          val operationPerformed: Future[ResponseConnGame] = handler.ask(CreateConnectionGame(_, gameId, nickname))
          onSuccess(operationPerformed) {
            case ServiceRoutes.SuccessConG(flow) => handleWebSocketMessages(flow)
            case ServiceRoutes.FailureConG(reason) => complete(StatusCodes.InternalServerError -> reason)
          }
        }
      }
    )
}
