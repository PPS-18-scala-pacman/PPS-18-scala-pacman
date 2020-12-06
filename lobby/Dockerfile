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
COPY ./lobby/src/main/resources/conf/config-docker.json /app/lobby/src/main/resources/conf/config.json
RUN ["./gradlew", "--no-daemon", ":lobby:shadowJar"]

# Stage 1, based on openjdk jre8, ready for production
FROM openjdk:${VERSION}-jre-alpine
COPY --from=BUILD /app/lobby/build/libs/*-fat.jar /app/lobby-fat.jar

# set the startup command to execute the jar
CMD ["java", "-d64", "-jar", "/app/lobby-fat.jar"]
EXPOSE 8081
