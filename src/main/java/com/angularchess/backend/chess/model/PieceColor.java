package com.angularchess.backend.chess.model;

public enum PieceColor {
	WHITE,
	BLACK;

	public PieceColor opponent() {
		return this == WHITE ? BLACK : WHITE;
	}
}
