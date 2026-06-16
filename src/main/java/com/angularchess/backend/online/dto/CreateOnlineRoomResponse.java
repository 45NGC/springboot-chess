package com.angularchess.backend.online.dto;

import com.angularchess.backend.online.model.OnlineRoom;
import com.angularchess.backend.online.model.OnlineRoomSession;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateOnlineRoomResponse(
	@NotNull @Valid OnlineRoom room,
	@NotNull @Valid OnlineRoomSession session
) {
}
