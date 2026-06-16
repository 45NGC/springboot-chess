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
- `notYourTurn`
- `finished`

## Reglas De Dominio

- Estados de sala: `waiting | ready | playing | finished`
- El host puede elegir `white | black | random`
- La sala pasa a `ready` cuando entra el segundo jugador
- La sala pasa a `playing` en el primer movimiento aceptado
- Solo puede mover el jugador al que le toca

## Limitacion Actual Del Backend

Esta primera version de Spring Boot aplica participantes, capacidad, turnos y ciclo de vida de la sala.

Todavia no valida toda la legalidad ajedrecistica y por ahora confia en que el frontend envie movimientos legales.
