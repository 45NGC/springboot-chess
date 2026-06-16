package com.angularchess.backend.online.dto;

import com.angularchess.backend.online.model.OnlineRoom;
import com.angularchess.backend.online.model.SubmitOnlineMoveError;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.Valid;

@JsonInclude(Include.NON_NULL)
public record SubmitOnlineMoveResponse(
	boolean ok,
	@Valid OnlineRoom room,
	SubmitOnlineMoveError error
) {
	public static SubmitOnlineMoveResponse success(OnlineRoom room) {
		return new SubmitOnlineMoveResponse(true, room, null);
	}

	public static SubmitOnlineMoveResponse failure(SubmitOnlineMoveError error) {
		return new SubmitOnlineMoveResponse(false, null, error);
	}
}
