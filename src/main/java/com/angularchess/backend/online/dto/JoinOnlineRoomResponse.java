package com.angularchess.backend.online.dto;

import com.angularchess.backend.online.model.JoinOnlineRoomError;
import com.angularchess.backend.online.model.OnlineRoom;
import com.angularchess.backend.online.model.OnlineRoomSession;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.Valid;

@JsonInclude(Include.NON_NULL)
public record JoinOnlineRoomResponse(
	boolean ok,
	@Valid OnlineRoom room,
	@Valid OnlineRoomSession session,
	JoinOnlineRoomError error
) {
	public static JoinOnlineRoomResponse success(OnlineRoom room, OnlineRoomSession session) {
		return new JoinOnlineRoomResponse(true, room, session, null);
	}

	public static JoinOnlineRoomResponse failure(JoinOnlineRoomError error) {
		return new JoinOnlineRoomResponse(false, null, null, error);
	}
}
