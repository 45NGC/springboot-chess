package com.angularchess.backend.chess.board;

public class CastlingRights {

	private boolean shortCastle;
	private boolean longCastle;

	public CastlingRights(boolean shortCastle, boolean longCastle) {
		this.shortCastle = shortCastle;
		this.longCastle = longCastle;
	}

	public boolean isShortCastle() {
		return shortCastle;
	}

	public void setShortCastle(boolean shortCastle) {
		this.shortCastle = shortCastle;
	}

	public boolean isLongCastle() {
		return longCastle;
	}

	public void setLongCastle(boolean longCastle) {
		this.longCastle = longCastle;
	}

	public CastlingRights copy() {
		return new CastlingRights(shortCastle, longCastle);
	}
}
