package it.unibo.scalapacman.common

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import it.unibo.scalapacman.lib.model.{Direction, Dot, Fruit}


class FruitType extends TypeReference[Fruit.type]
case class FruitHolder(@JsonScalaEnumeration(classOf[FruitType]) fruit: Fruit.Value)

class DotType extends TypeReference[Dot.type]
case class DotHolder(@JsonScalaEnumeration(classOf[DotType]) dot: Dot.Value)

class GameCharacterType extends TypeReference[GameCharacter.type]
case class GameCharacterHolder(@JsonScalaEnumeration(classOf[GameCharacterType]) gameChar: GameCharacter.Value)

class DirectionType extends TypeReference[Direction.type]
case class DirectionHolder(@JsonScalaEnumeration(classOf[DirectionType]) direction: Direction.Value)
