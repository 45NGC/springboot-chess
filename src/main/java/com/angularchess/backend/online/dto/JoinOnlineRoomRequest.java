package com.angularchess.backend.online.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinOnlineRoomRequest(
	@NotBlank String code
) {
}
