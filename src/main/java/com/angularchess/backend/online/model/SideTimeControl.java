package com.angularchess.backend.online.model;

import jakarta.validation.constraints.Min;

public record SideTimeControl(
	@Min(0) int baseMinutes,
	@Min(0) int incrementSeconds
) {
}
