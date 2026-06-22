package com.angularchess.backend.chess.board;

public class CastlingAvailability {

	private CastlingRights white = new CastlingRights(true, true);
	private CastlingRights black = new CastlingRights(true, true);

	public CastlingRights getWhite() {
		return white;
	}

	public void setWhite(CastlingRights white) {
		this.white = white;
	}

	public CastlingRights getBlack() {
		return black;
	}

	public void setBlack(CastlingRights black) {
		this.black = black;
	}

	public CastlingAvailability copy() {
		CastlingAvailability copy = new CastlingAvailability();
		copy.setWhite(white.copy());
		copy.setBlack(black.copy());
		return copy;
	}
}
