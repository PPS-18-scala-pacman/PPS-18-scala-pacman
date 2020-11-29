package it.unibo.scalapacman.server.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import it.unibo.scalapacman.lib.model.PacmanType
import spray.json._ // scalastyle:ignore

case class CreateGameRequest(components: Map[String, PacmanType.PacmanType])

object CreateGameJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object ComponentsJsonFormat extends RootJsonFormat[Map[String, PacmanType.PacmanType]] {

    def write(obj: Map[String, PacmanType.PacmanType]): JsValue =
      JsObject(obj.map(item => item._1 -> JsNumber(PacmanType.playerTypeValToIndex(item._2)) ))

    def read(value: JsValue): Map[String, PacmanType.PacmanType] = value match {
      case x: JsObject => x.fields match {
        case x: Map[String, JsValue] => x transform {
          case (_: String, pacmanType: JsNumber) => PacmanType.indexToPlayerTypeVal(pacmanType.value.intValue())
          case _ => null  // scalastyle:ignore
        }
      }
      case _ => null  // scalastyle:ignore
    }
  }

  implicit val createGameRequestFormat: RootJsonFormat[CreateGameRequest] = jsonFormat1(CreateGameRequest)
}
