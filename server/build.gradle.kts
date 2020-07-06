// It is possible to import classes
//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  application
  // fat jar
  id("com.github.johnrengelman.shadow") version "5.2.0"
}

val group: String by project

dependencies {
  implementation(project(":pacman-lib"))
}

application {
  mainClassName = "$group.server.ServerApp"
}

tasks.shadowJar {
  archiveClassifier.set("fat")
}
