package com.angularchess.backend.online.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OnlineRoomSide {
	WHITE("white"),
	BLACK("black");

	private final String value;

	OnlineRoomSide(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static OnlineRoomSide fromValue(String value) {
		for (OnlineRoomSide side : values()) {
			if (side.value.equals(value)) {
				return side;
			}
		}
		throw new IllegalArgumentException("Unknown online room side: " + value);
	}
}
