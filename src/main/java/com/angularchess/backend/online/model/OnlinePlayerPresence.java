package com.angularchess.backend.online.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OnlinePlayerPresence {
	CONNECTED("connected"),
	DISCONNECTED("disconnected");

	private final String value;

	OnlinePlayerPresence(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static OnlinePlayerPresence fromValue(String value) {
		for (OnlinePlayerPresence presence : values()) {
			if (presence.value.equals(value)) {
				return presence;
			}
		}
		throw new IllegalArgumentException("Unknown online player presence: " + value);
	}
}
