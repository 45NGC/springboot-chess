package com.angularchess.backend.online.model;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record OnlineRoom(
	@NotBlank String code,
	@NotNull OnlineRoomStatus status,
	@Valid OnlineRoomPlayer whitePlayer,
	@Valid OnlineRoomPlayer blackPlayer,
	@NotNull @Valid TimeControl timeControlSettings,
	@PositiveOrZero long whiteTimeMs,
	@PositiveOrZero long blackTimeMs,
	@Valid OnlineRoomSide activeClockColor,
	@PositiveOrZero Long clockUpdatedAt,
	@Valid OnlineRoomSide timeoutWinner,
	boolean whiteRequestedRematch,
	boolean blackRequestedRematch,
	@NotNull List<@Valid OnlineMoveRecord> moves,
	@PositiveOrZero long createdAt,
	Long startedAt,
	Long finishedAt
) {
}
