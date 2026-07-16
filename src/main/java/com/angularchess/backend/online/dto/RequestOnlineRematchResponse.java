package com.angularchess.backend.online.dto;

import com.angularchess.backend.online.model.OnlineRoom;
import com.angularchess.backend.online.model.RequestOnlineRematchError;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.Valid;

@JsonInclude(Include.NON_NULL)
public record RequestOnlineRematchResponse(
	boolean ok,
	@Valid OnlineRoom room,
	RequestOnlineRematchError error
) {
	public static RequestOnlineRematchResponse success(OnlineRoom room) {
		return new RequestOnlineRematchResponse(true, room, null);
	}

	public static RequestOnlineRematchResponse failure(RequestOnlineRematchError error) {
		return new RequestOnlineRematchResponse(false, null, error);
	}
}
