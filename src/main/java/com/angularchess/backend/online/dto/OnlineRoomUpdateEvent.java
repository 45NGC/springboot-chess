package com.angularchess.backend.online.dto;

import com.angularchess.backend.online.model.OnlineRoom;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OnlineRoomUpdateEvent(
	@NotNull @Valid OnlineRoom room
) {
}
