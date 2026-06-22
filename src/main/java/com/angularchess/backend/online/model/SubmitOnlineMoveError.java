package com.angularchess.backend.online.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SubmitOnlineMoveError {
	NOT_FOUND("notFound"),
	NOT_PARTICIPANT("notParticipant"),
	ILLEGAL_MOVE("illegalMove"),
	NOT_YOUR_TURN("notYourTurn"),
	FINISHED("finished");

	private final String value;

	SubmitOnlineMoveError(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static SubmitOnlineMoveError fromValue(String value) {
		for (SubmitOnlineMoveError error : values()) {
			if (error.value.equals(value)) {
				return error;
			}
		}
		throw new IllegalArgumentException("Unknown submit move error: " + value);
	}
}
