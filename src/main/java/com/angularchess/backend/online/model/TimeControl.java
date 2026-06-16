package com.angularchess.backend.online.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TimeControl(
	@NotNull @Valid SideTimeControl white,
	@NotNull @Valid SideTimeControl black
) {
}
