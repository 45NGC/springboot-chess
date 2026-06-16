package com.angularchess.backend.online.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JoinOnlineRoomError {
	NOT_FOUND("notFound"),
	FULL("full"),
	FINISHED("finished");

	private final String value;

	JoinOnlineRoomError(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static JoinOnlineRoomError fromValue(String value) {
		for (JoinOnlineRoomError error : values()) {
			if (error.value.equals(value)) {
				return error;
			}
		}
		throw new IllegalArgumentException("Unknown join room error: " + value);
	}
}
