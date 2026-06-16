package com.angularchess.backend.online.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record OnlineRoomPlayer(
	@NotBlank String id,
	@NotNull OnlineRoomSide side,
	@NotNull OnlinePlayerPresence presence,
	@PositiveOrZero long joinedAt
) {
}
