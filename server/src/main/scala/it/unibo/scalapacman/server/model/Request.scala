package it.unibo.scalapacman.server.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import it.unibo.scalapacman.lib.model.PacmanType.{PacmanType, PACMAN, RAPMAN, MS_PACMAN, CAPMAN}
import spray.json._ // scalastyle:ignore

case class GameComponent(nickname: String, pacmanType: PacmanType)
//case class CreateGameRequest(components: List[GameComponent])
case class CreateGameRequest(components: Map[String, PacmanType])   //TODO L meglio rispetto a sopra???

//TODO L  da usare in futuro
object JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val createPacmanTypeFormat: RootJsonFormat[PacmanType] = jsonFormat0(PacmanType)
  implicit val createGameComponentFormat: RootJsonFormat[GameComponent] = jsonFormat2(GameComponent)
  implicit val createGameRequestFormat: RootJsonFormat[CreateGameRequest] = jsonFormat1(CreateGameRequest)
}

//TODO L da rimuovere
object RequestMockUp {
  def getComponentDefault(playerNumber: Int): List[GameComponent] = playerNumber match {
    case 1 => List(GameComponent("GioL", PACMAN))
    case 2 => List(GameComponent("pacman2", PACMAN), GameComponent("pacmina2", MS_PACMAN))
    case 3 => List(GameComponent("pacman", PACMAN), GameComponent("pacmina", MS_PACMAN), GameComponent("rapman", RAPMAN))
    case _ => List(GameComponent("pacman4", PACMAN), GameComponent("pacmina4", MS_PACMAN), GameComponent("rapman4", RAPMAN),
      GameComponent("capman4", CAPMAN))
  }
}
