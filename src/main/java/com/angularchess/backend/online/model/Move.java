package com.angularchess.backend.online.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record Move(
	@Min(0) @Max(63) int from,
	@Min(0) @Max(63) int to,
	PromotionPiece promotion,
	Boolean enPassant,
	CastlingType castling,
	Boolean doublePush
) {
}
