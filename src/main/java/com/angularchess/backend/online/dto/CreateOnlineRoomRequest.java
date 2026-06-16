package com.angularchess.backend.online.dto;

import com.angularchess.backend.online.model.OnlineGameSettings;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateOnlineRoomRequest(
	@NotNull @Valid OnlineGameSettings settings
) {
}
