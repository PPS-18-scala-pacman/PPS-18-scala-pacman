import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  java
  application
  // fat jar
  id("com.github.johnrengelman.shadow") version "5.2.0"
}

val vertxVersion = "3.9.4"
val junitVersion = "5.3.2"

dependencies {
  implementation(group = "io.vertx", name = "vertx-core", version = vertxVersion)
  implementation(group = "io.vertx", name = "vertx-config", version = vertxVersion)
  implementation(group = "io.vertx", name = "vertx-web", version = vertxVersion)
  implementation(group = "io.vertx", name = "vertx-pg-client", version = vertxVersion)
  implementation(group = "io.vertx", name = "vertx-rx-java", version = vertxVersion)

  testImplementation(group = "io.vertx", name = "vertx-junit5", version = vertxVersion)
  testImplementation(group = "io.vertx", name = "vertx-web-client", version = vertxVersion)
  testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = junitVersion)
  testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = junitVersion)
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
}

application {
  mainClassName = "io.vertx.core.Launcher"
}

val mainVerticleName = "$group.lobby.MainVerticle"
val watchForChange = "src/**/*.java"
val doOnChange = "${projectDir}/../gradlew classes"

tasks {
  test {
    useJUnitPlatform()
  }

  getByName<JavaExec>("run") {
    args = listOf("run", mainVerticleName, "--redeploy=${watchForChange}", "--launcher-class=${application.mainClassName}", "--on-redeploy=${doOnChange}")
  }

  withType<ShadowJar> {
    manifest {
      attributes["Main-Verticle"] = mainVerticleName
    }
    mergeServiceFiles {
      include("META-INF/services/io.vertx.core.spi.VerticleFactory")
    }
  }
}
