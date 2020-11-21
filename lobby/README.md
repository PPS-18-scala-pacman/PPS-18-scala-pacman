# Lobby microservice

## Avvio

### Requisiti

Postgresql avviato in localhost sulla porta 5432.

È possibile avviarlo con docker lanciando `docker-compose up` dalla root del progetto.

### Avvio con Gradle

```
./gradlew :lobby:run
```

### Configurazione di avvio Intellij

Creare una configurazione di avvio di tipo "Application" con le seguenti configurazioni:

```
Main class: io.vertx.core.Launcher
Program arguments: run it.unibo.scalapacman.lobby.MainVerticle
Use classpath of module: scalapacman.lobby.main
```

## Test

### SSE

Per testare i Server-Sent Events è possibile eseguire i seguenti comandi da un terminale linux.

Per la get all:

```bash
curl -v -X GET http://localhost:8080/api/lobby -H "Content-Type: text/event-stream"
```

Per la get by id:

```bash
curl -v -X GET http://localhost:8080/api/lobby/4 -H "Content-Type: text/event-stream"
```
