// Esempio build Akka https://github.com/beercan1989/playground-kotlin-akka/blob/master/build.gradle.kts
// It is possible to import classes

plugins {
  scala
  idea
  id("com.github.maiflai.scalatest") version "0.26"
  id("org.scoverage") version "4.0.1"
}

allprojects {
  repositories {
    // is superior to mavenCentral() in terms of performance and memory footprint
    // https://stackoverflow.com/a/50726436/3687018
    /*jcenter()*/
    mavenCentral()
  }

  apply(plugin = "scala")
  apply(plugin = "com.github.maiflai.scalatest")
  apply(plugin = "org.scoverage")

  val scalaVersion = "2.12"
  val akkaVersion = "2.6.5"
  val akkaHttpVersion = "10.1.12"
  val scoverageVersion = "1.4.1"

  dependencies {
    implementation(group = "org.scala-lang", name = "scala-library", version = "$scalaVersion.8")
    implementation(group = "it.unibo.alice.tuprolog", name = "tuprolog", version = "3.3.0")

    implementation(group = "com.typesafe.akka", name = "akka-actor-typed_$scalaVersion", version = akkaVersion)
    implementation(group = "com.typesafe.akka", name = "akka-stream_$scalaVersion", version = akkaVersion)
    implementation(group = "com.typesafe.akka", name = "akka-http_$scalaVersion", version = akkaHttpVersion)
    implementation(group = "com.typesafe.akka", name = "akka-http-spray-json_$scalaVersion", version = akkaHttpVersion)
    /*implementation(group = "com.typesafe.akka", name = "akka-protobuf_$scalaVersion", version = akkaVersion)*/

    // Logging
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
    implementation(group = "com.typesafe.akka", name = "akka-slf4j_$scalaVersion", version = akkaVersion)

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
}

subprojects {
}

tasks.register("release") {
//  dependsOn(tasks.test)
//  dependsOn(tasks.scalatest)
}
