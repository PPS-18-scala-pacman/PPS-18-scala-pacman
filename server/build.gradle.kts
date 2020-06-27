// It is possible to import classes
//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  application
  // fat jar
  id("com.github.johnrengelman.shadow") version "5.2.0"
}

dependencies {
  implementation(project(":pacman-lib"))
}

application {
  mainClassName = "it.unibo.scalapacman.server.ServerApp"
}

tasks.shadowJar {
  archiveClassifier.set("fat")
}
