package it.unibo.scalapacman.client.utility

sealed trait View { def name: String }

object View {
  case object MENU extends View { val name = "menu" }
  case object PLAY extends View { val name = "play" }
  case object OPTIONS extends View { val name = "options" }
  case object STATS extends View { val name = "stats" }
}
