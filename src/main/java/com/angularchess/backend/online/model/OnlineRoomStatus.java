package com.angularchess.backend.online.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OnlineRoomStatus {
	WAITING("waiting"),
	READY("ready"),
	PLAYING("playing"),
	FINISHED("finished");

	private final String value;

	OnlineRoomStatus(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static OnlineRoomStatus fromValue(String value) {
		for (OnlineRoomStatus status : values()) {
			if (status.value.equals(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown online room status: " + value);
	}
}
