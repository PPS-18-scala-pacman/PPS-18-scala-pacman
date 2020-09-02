// It is possible to import classes
//import com.github.jengelman.gradle.plugins.shadow.tasks.shadowJar

plugins {
  application
  // fat jar
  id("com.github.johnrengelman.shadow") version "5.2.0"
}

val scalaVersion: String by project
val group: String by project

dependencies {
  implementation(project(":pacman-lib"))
  implementation(project(":common"))

//  testImplementation(group = "org.scalamock", name = "scalamock-scalatest-support_$scalaVersion", version = "3.6.0")
  testImplementation(group = "org.scalamock", name = "scalamock_$scalaVersion", version = "4.4.0")
}

application {
  mainClassName = "$group.client.ClientApp"
}

tasks.shadowJar {
  archiveClassifier.set("fat")

  // Akka reference.conf resource file
  val newTransformer = com.github.jengelman.gradle.plugins.shadow.transformers.AppendingTransformer()
  newTransformer.resource = "reference.conf"
  transformers.add(newTransformer)
}

scoverage {
  excludedPackages.set(listOf("it.unibo.scalapacman.client.gui"))
  minimumRate.set(BigDecimal("0.6"))
}
