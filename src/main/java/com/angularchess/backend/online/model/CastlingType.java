package com.angularchess.backend.online.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CastlingType {
	KING_SIDE("kingSide"),
	QUEEN_SIDE("queenSide");

	private final String value;

	CastlingType(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static CastlingType fromValue(String value) {
		for (CastlingType type : values()) {
			if (type.value.equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown castling type: " + value);
	}
}
