package com.angularchess.backend.chess;

import com.angularchess.backend.chess.board.Board;
import com.angularchess.backend.chess.board.SquareUtils;
import com.angularchess.backend.chess.model.Piece;
import com.angularchess.backend.chess.model.PieceColor;
import com.angularchess.backend.chess.model.PieceType;

public final class TestBoards {

	private TestBoards() {
	}

	public static Board emptyBoard() {
		return new Board();
	}

	public static Piece white(PieceType type) {
		return new Piece(type, PieceColor.WHITE);
	}

	public static Piece black(PieceType type) {
		return new Piece(type, PieceColor.BLACK);
	}

	public static void addKings(Board board) {
		board.set(SquareUtils.toIndex(0, 4), white(PieceType.KING));
		board.set(SquareUtils.toIndex(7, 4), black(PieceType.KING));
	}
}
