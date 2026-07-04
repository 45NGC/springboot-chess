# Online Backend Contract

This backend follows the first online contract defined for `angular-chess`.

## REST Endpoints

### `POST /api/online/rooms`

Creates a room.

Request body:

```json
{
  "settings": {
    "timeControlSettings": {
      "white": { "baseMinutes": 5, "incrementSeconds": 0 },
      "black": { "baseMinutes": 5, "incrementSeconds": 0 }
    },
    "hostSidePreference": "random"
  }
}
```

Response body:

```json
{
  "room": {},
  "session": {}
}
```

### `POST /api/online/rooms/{code}/join`

Joins a room.

Request body:

```json
{
  "code": "ABC123"
}
```

Success response:

```json
{
  "ok": true,
  "room": {},
  "session": {}
}
```

Failure response:

```json
{
  "ok": false,
  "error": "notFound"
}
```

Possible errors:

- `notFound`
- `full`
- `finished`

### `GET /api/online/rooms/{code}`

Returns the current room snapshot.

Response body:

```json
{
  "room": {}
}
```

If the room does not exist:

```json
{
  "room": null
}
```

### `POST /api/online/rooms/{code}/moves`

Submits one move.

Request body:

```json
{
  "playerId": "player_xxxx",
  "move": {
    "from": 12,
    "to": 28
  }
}
```

Success response:

```json
{
  "ok": true,
  "room": {}
}
```

Failure response:

```json
{
  "ok": false,
  "error": "notYourTurn"
}
```

Possible errors:

- `notFound`
- `notParticipant`
- `illegalMove`
- `notYourTurn`
- `finished`

## Domain Rules

- Room statuses: `waiting | ready | playing | finished`
- The host can choose `white | black | random`
- A room becomes `ready` when a second player joins
- A room becomes `playing` on the first accepted move
- A room becomes `finished` when the backend detects checkmate, stalemate or a supported draw rule
- Only the player whose turn it is can move

## WebSocket Updates

STOMP endpoint:

- `/ws`

Room subscription destination:

- `/topic/online/rooms/{code}`

Published payload:

```json
{
  "room": {}
}
```

The backend publishes the full room snapshot after:

- room creation
- successful room join
- each accepted move

## Current Backend Status

This Spring Boot version enforces participants, capacity, turn order, legal chess moves and room lifecycle.

It already validates legal moves on the backend, computes terminal positions from the accepted move history, and broadcasts updated room snapshots over WebSocket.
