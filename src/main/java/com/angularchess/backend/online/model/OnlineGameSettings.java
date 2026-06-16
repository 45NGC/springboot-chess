package com.angularchess.backend.online.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OnlineGameSettings(
	@NotNull @Valid TimeControl timeControlSettings,
	@NotNull HostSidePreference hostSidePreference
) {
}
