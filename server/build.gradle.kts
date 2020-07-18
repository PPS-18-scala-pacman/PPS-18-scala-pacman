import com.github.jengelman.gradle.plugins.shadow.transformers.AppendingTransformer

plugins {
  application
  // fat jar
  id("com.github.johnrengelman.shadow") version "5.2.0"
}

val group: String by project

dependencies {
  implementation(project(":pacman-lib"))
  implementation(project(":common"))
}

application {
  mainClassName = "$group.server.ServerApp"
}

tasks.shadowJar {
  archiveClassifier.set("fat")

  // Akka reference.conf resource file
  val newTransformer = AppendingTransformer()
  newTransformer.resource = "reference.conf"
  transformers.add(newTransformer)
}

scoverage {
  minimumRate.set(BigDecimal("0.5"))
}
