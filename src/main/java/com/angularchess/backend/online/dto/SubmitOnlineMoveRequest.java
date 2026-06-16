package com.angularchess.backend.online.dto;

import com.angularchess.backend.online.model.Move;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitOnlineMoveRequest(
	@NotBlank String playerId,
	@NotNull @Valid Move move
) {
}
