package com.angularchess.backend.online.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RequestOnlineRematchError {
	NOT_FOUND("notFound"),
	NOT_PARTICIPANT("notParticipant"),
	NOT_FINISHED("notFinished");

	private final String value;

	RequestOnlineRematchError(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static RequestOnlineRematchError fromValue(String value) {
		for (RequestOnlineRematchError error : values()) {
			if (error.value.equals(value)) {
				return error;
			}
		}
		throw new IllegalArgumentException("Unknown rematch error: " + value);
	}
}
