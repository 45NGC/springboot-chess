package com.angularchess.backend.chess.rules;

import java.util.HashSet;
import java.util.Set;

import com.angularchess.backend.chess.board.Board;
import com.angularchess.backend.chess.board.SquareUtils;
import com.angularchess.backend.chess.model.Piece;
import com.angularchess.backend.chess.model.PieceColor;

public final class DrawRules {

	private DrawRules() {
	}

	public static boolean isInsufficientMaterial(Board board) {
		int bishops = 0;
		int knights = 0;
		int whiteBishops = 0;
		int blackBishops = 0;
		int whiteKnights = 0;
		int blackKnights = 0;
		Set<String> bishopColors = new HashSet<>();

		for (int square = 0; square < board.getSquares().length; square++) {
			Piece piece = board.get(square);
			if (piece == null) {
				continue;
			}

			switch (piece.type()) {
				case PAWN, ROOK, QUEEN -> {
					return false;
				}
				case BISHOP -> {
					bishops++;
					if (piece.color() == PieceColor.WHITE) {
						whiteBishops++;
					} else {
						blackBishops++;
					}
					bishopColors.add(squareColor(square));
				}
				case KNIGHT -> {
					knights++;
					if (piece.color() == PieceColor.WHITE) {
						whiteKnights++;
					} else {
						blackKnights++;
					}
				}
				case KING -> {
				}
			}
		}

		int minorPieces = bishops + knights;
		if (minorPieces == 0 || minorPieces == 1) {
			return true;
		}

		int whiteMinors = whiteBishops + whiteKnights;
		int blackMinors = blackBishops + blackKnights;
		if (whiteMinors == 1 && blackMinors == 1) {
			return true;
		}

		return knights == 0 && bishops > 0 && bishopColors.size() == 1;
	}

	private static String squareColor(int square) {
		int rank = SquareUtils.rankOf(square);
		int file = SquareUtils.fileOf(square);
		return (rank + file) % 2 == 0 ? "dark" : "light";
	}
}
