package com.angularchess.backend.online.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PromotionPiece {
	QUEEN("queen"),
	ROOK("rook"),
	BISHOP("bishop"),
	KNIGHT("knight");

	private final String value;

	PromotionPiece(String value) {
		this.value = value;
	}

	@JsonValue
	public String value() {
		return value;
	}

	@JsonCreator
	public static PromotionPiece fromValue(String value) {
		for (PromotionPiece piece : values()) {
			if (piece.value.equals(value)) {
				return piece;
			}
		}
		throw new IllegalArgumentException("Unknown promotion piece: " + value);
	}
}
