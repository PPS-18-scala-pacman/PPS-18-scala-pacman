import com.github.alisiikh.scalastyle.ScalastyleExtension

val rootScalastyleConfig = file("${projectDir}/scalastyle_config.xml")

plugins {
  scala
  idea
  id("com.github.maiflai.scalatest") version "0.26"
  id("org.scoverage") version "4.0.1"
  id("com.github.alisiikh.scalastyle") version "3.4.0"
}

buildscript {
  repositories {
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath("com.github.alisiikh:gradle-scalastyle-plugin:3.4.0")
  }
}

allprojects {
  repositories {
    mavenCentral()
  }

  apply(plugin = "com.github.alisiikh.scalastyle")

  apply(plugin = "scala")
  apply(plugin = "com.github.maiflai.scalatest")
  apply(plugin = "org.scoverage")

  val scalaVersion: String by project
  val akkaVersion: String by project
  val akkaHttpVersion: String by project
  val scoverageVersion: String by project

  dependencies {
    implementation(group = "org.scala-lang", name = "scala-library", version = "$scalaVersion.8")
    implementation(group = "it.unibo.alice.tuprolog", name = "tuprolog", version = "3.3.0")

    implementation(group = "com.typesafe.akka", name = "akka-actor-typed_$scalaVersion", version = akkaVersion)
    implementation(group = "com.typesafe.akka", name = "akka-stream_$scalaVersion", version = akkaVersion)
    implementation(group = "com.typesafe.akka", name = "akka-stream-typed_$scalaVersion", version = akkaVersion)
    implementation(group = "com.typesafe.akka", name = "akka-http_$scalaVersion", version = akkaHttpVersion)
    implementation(group = "com.typesafe.akka", name = "akka-http-spray-json_$scalaVersion", version = akkaHttpVersion)

    // Logging
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
    implementation(group = "com.typesafe.akka", name = "akka-slf4j_$scalaVersion", version = akkaVersion)
    implementation(group = "org.clapper", name = "grizzled-slf4j_$scalaVersion", version = "1.3.4")

    testImplementation(group = "org.scalatest", name = "scalatest_$scalaVersion", version = "3.1.2")
    testImplementation(group = "com.typesafe.akka", name = "akka-actor-testkit-typed_$scalaVersion", version = akkaVersion)
    testImplementation(group = "com.typesafe.akka", name = "akka-http-testkit_$scalaVersion", version = akkaHttpVersion)
    testImplementation(group = "com.typesafe.akka", name = "akka-stream-testkit_$scalaVersion", version = akkaVersion)

    testRuntimeOnly(group = "com.vladsch.flexmark", name = "flexmark-all", version = "0.36.8")

    scoverage(group = "org.scoverage", name = "scalac-scoverage-plugin_$scalaVersion", version = scoverageVersion)
    scoverage(group = "org.scoverage", name = "scalac-scoverage-runtime_$scalaVersion", version = scoverageVersion)
  }

  tasks.withType<ScalaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }

  configure<ScalastyleExtension> {
    config.set(rootScalastyleConfig)
    failOnWarning.set(true)
  }
}

subprojects {
}

tasks.register("release") {
//  dependsOn(tasks.test)
//  dependsOn(tasks.scalatest)
}
