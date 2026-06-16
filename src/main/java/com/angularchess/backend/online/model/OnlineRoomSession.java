package com.angularchess.backend.online.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OnlineRoomSession(
	@NotBlank String roomCode,
	@NotBlank String playerId,
	@NotNull OnlineRoomSide playerSide
) {
}
