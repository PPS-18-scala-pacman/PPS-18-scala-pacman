# Lobby microservice

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
