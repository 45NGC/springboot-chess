# Contrato Del Backend Online

Este backend sigue el primer contrato online definido para `angular-chess`.

## Endpoints REST

### `POST /api/online/rooms`

Crea una sala.

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

Se une a una sala.

Request body:

```json
{
  "code": "ABC123"
}
```

Response body en caso de exito:

```json
{
  "ok": true,
  "room": {},
  "session": {}
}
```

Response body en caso de error:

```json
{
  "ok": false,
  "error": "notFound"
}
```

Errores posibles:

- `notFound`
- `full`
- `finished`

### `GET /api/online/rooms/{code}`

Devuelve el snapshot actual de la sala.

Response body:

```json
{
  "room": {}
}
```

Si la sala no existe:

```json
{
  "room": null
}
```

### `POST /api/online/rooms/{code}/moves`

Envia un movimiento.

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

Response body en caso de exito:

```json
{
  "ok": true,
  "room": {}
}
```

Response body en caso de error:

```json
{
  "ok": false,
  "error": "notYourTurn"
}
```

Errores posibles:

- `notFound`
- `notParticipant`
- `illegalMove`
- `notYourTurn`
- `finished`

## Reglas De Dominio

- Estados de sala: `waiting | ready | playing | finished`
- El host puede elegir `white | black | random`
- La sala pasa a `ready` cuando entra el segundo jugador
- La sala pasa a `playing` en el primer movimiento aceptado
- La sala pasa a `finished` cuando el backend detecta jaque mate, ahogado o unas tablas soportadas
- Solo puede mover el jugador al que le toca

## Actualizaciones Por WebSocket

Endpoint STOMP:

- `/ws`

Destino de suscripcion por sala:

- `/topic/online/rooms/{code}`

Payload publicado:

```json
{
  "room": {}
}
```

El backend publica el snapshot completo de la sala tras:

- crear la sala
- unirse correctamente a la sala
- cada movimiento aceptado

## Estado Actual Del Backend

Esta version de Spring Boot aplica participantes, capacidad, turnos, legalidad real de movimientos y ciclo de vida de la sala.

Ya valida movimientos legales en el backend, calcula posiciones terminales a partir del historial de jugadas aceptadas y emite snapshots actualizados de la sala por WebSocket.
