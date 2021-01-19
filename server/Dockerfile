ARG VERSION=8

# Stage 0, based on openjdk jdk8, to build and compile
FROM openjdk:${VERSION}-jdk-alpine as BUILD

# Get gradle distribution
COPY *.gradle gradle.* gradlew /app/
COPY gradle /app/gradle
WORKDIR /app
RUN ["./gradlew", "--version"]

# Build the project
COPY ./ /app/
RUN ["./gradlew", "--no-daemon", ":server:shadowJar"]

# Stage 1, based on openjdk jre8, ready for production
FROM openjdk:${VERSION}-jre-alpine
COPY --from=BUILD /app/server/build/libs/*-fat.jar /app/server-fat.jar

# set the startup command to execute the jar
CMD ["java", "-d64", "-jar", "/app/server-fat.jar"]
EXPOSE 8080
