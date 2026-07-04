# springboot-chess

Spring Boot backend for [angular-chess](https://github.com/45NGC/angular-chess), focused first on the online mode REST contract.

## Current Scope

This first version implements the basic online room flow in memory:

- create room
- join room by code
- fetch room snapshot
- submit move

Implemented endpoints:

- `POST /api/online/rooms`
- `POST /api/online/rooms/{code}/join`
- `GET /api/online/rooms/{code}`
- `POST /api/online/rooms/{code}/moves`

## Initial Architecture

The project is intentionally simple for the first iteration:

- `online/controller`: REST endpoints
- `online/dto`: request/response payloads aligned with the Angular contract
- `online/model`: shared domain objects and enums
- `online/service`: room lifecycle and turn rules
- `online/repository`: in-memory storage with `ConcurrentHashMap`
- `config/WebConfig`: CORS for local Angular development

The service is already structured so the next steps are straightforward:

- replace the in-memory repository with persistence
- add clocks, reconnection and session hardening

## Important v1 Limitation

This version enforces:

- room existence
- room capacity
- room status transitions
- participant ownership
- turn order
- legal chess moves on the backend
- game end detection from the move history

The backend now validates moves authoritatively instead of trusting the frontend.

Current limitations:

- no database persistence yet
- no reconnection/session hardening yet

## Run

Requirements:

- Java 21

Then:

```bash
./mvnw spring-boot:run
```

By default the backend allows CORS from:

- `http://localhost:4200`

You can change it in `src/main/resources/application.properties`.

## WebSocket

The backend exposes a STOMP WebSocket endpoint at:

- `/ws`

Clients can subscribe to room updates at:

- `/topic/online/rooms/{code}`

Published payload:

```json
{
  "room": {}
}
```

The backend publishes a new room snapshot when:

- a room is created
- a second player joins
- a move is accepted

## Example Requests

Create room:

```bash
curl -X POST http://localhost:8080/api/online/rooms \
  -H 'Content-Type: application/json' \
  -d '{
    "settings": {
      "timeControlSettings": {
        "white": { "baseMinutes": 5, "incrementSeconds": 0 },
        "black": { "baseMinutes": 5, "incrementSeconds": 0 }
      },
      "hostSidePreference": "random"
    }
  }'
```

Join room:

```bash
curl -X POST http://localhost:8080/api/online/rooms/ABC123/join \
  -H 'Content-Type: application/json' \
  -d '{ "code": "ABC123" }'
```

Get room:

```bash
curl http://localhost:8080/api/online/rooms/ABC123
```

Submit move:

```bash
curl -X POST http://localhost:8080/api/online/rooms/ABC123/moves \
  -H 'Content-Type: application/json' \
  -d '{
    "playerId": "player_abcd1234",
    "move": {
      "from": 12,
      "to": 28
    }
  }'
```

## Frontend Integration

To connect `angular-chess` later, the cleanest next step is:

1. Replace the current mock `OnlineRoomService` with an HTTP-backed implementation.
2. Keep the same frontend interfaces already defined in Angular.
3. Map the service methods directly to these backend calls:

- `createRoom(settings)` -> `POST /api/online/rooms`
- `joinRoom(code)` -> `POST /api/online/rooms/{code}/join`
- `getRoom(code)` -> `GET /api/online/rooms/{code}`
- `submitMove(code, playerId, move)` -> `POST /api/online/rooms/{code}/moves`

For realtime sync, connect to `/ws` and subscribe to `/topic/online/rooms/{code}` after creating or joining the room. Each event contains the full updated room snapshot, so the frontend can simply replace its local room state.

Note: invalid chess moves are now rejected with:

- `error: "illegalMove"`
