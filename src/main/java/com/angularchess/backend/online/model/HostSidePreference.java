package com.angularchess.backend.online.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HostSidePreference {
	WHITE("white"),
	BLACK("black"),
	RANDOM("random");

	private final String value;

	HostSidePreference(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static HostSidePreference fromValue(String value) {
		for (HostSidePreference preference : values()) {
			if (preference.value.equals(value)) {
				return preference;
			}
		}
		throw new IllegalArgumentException("Unknown host side preference: " + value);
	}
}
