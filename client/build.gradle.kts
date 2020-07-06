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

  testImplementation(group = "org.scalamock", name = "scalamock-scalatest-support_$scalaVersion", version = "3.6.0")
}

application {
  mainClassName = "$group.client.ClientApp"
}

tasks.shadowJar {
  archiveClassifier.set("fat")
}

scoverage {
  excludedPackages.set(listOf("it.unibo.scalapacman.client.gui"))
}
