plugins {
  id("org.springframework.boot") version "2.4.0-SNAPSHOT"
  id("io.spring.dependency-management") version "1.0.10.RELEASE"
  java
}

configurations {
	compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

repositories {
  mavenCentral()
  maven { url = uri("https://repo.spring.io/milestone") }
  maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
  implementation(group = "org.springframework.boot", name = "spring-boot-starter-data-jpa")
  implementation(group = "org.springframework.boot", name = "spring-boot-starter-web")
  compileOnly(group = "org.projectlombok", name = "lombok")
  developmentOnly(group = "org.springframework.boot", name = "spring-boot-devtools")
  runtimeOnly(group = "org.postgresql", name = "postgresql")
  annotationProcessor(group = "org.projectlombok", name = "lombok")
  testImplementation(group = "org.springframework.boot", name = "spring-boot-starter-test")
}

//test {
//	useJUnitPlatform()
//}
