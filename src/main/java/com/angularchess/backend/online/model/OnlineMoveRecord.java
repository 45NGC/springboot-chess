package com.angularchess.backend.online.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record OnlineMoveRecord(
	@NotNull @Valid Move move,
	@NotNull OnlineRoomSide playedBy,
	@PositiveOrZero long playedAt
) {
}
