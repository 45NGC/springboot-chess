package com.angularchess.backend.chess.rules;

import java.util.HashSet;
import java.util.Set;

import com.angularchess.backend.chess.board.Board;
import com.angularchess.backend.chess.board.SquareUtils;
import com.angularchess.backend.chess.model.Piece;
import com.angularchess.backend.chess.model.PieceColor;

public final class AttackedSquares {

	private static final int[][] KNIGHT_OFFSETS = {
		{2, 1}, {2, -1}, {-2, 1}, {-2, -1},
		{1, 2}, {1, -2}, {-1, 2}, {-1, -2}
	};
	private static final int[][] KING_OFFSETS = {
		{-1, -1}, {-1, 0}, {-1, 1},
		{0, -1}, {0, 1},
		{1, -1}, {1, 0}, {1, 1}
	};
	private static final int[][] BISHOP_DIRECTIONS = {
		{1, 1}, {1, -1}, {-1, 1}, {-1, -1}
	};
	private static final int[][] ROOK_DIRECTIONS = {
		{1, 0}, {-1, 0}, {0, 1}, {0, -1}
	};
	private static final int[][] QUEEN_DIRECTIONS = {
		{1, 1}, {1, -1}, {-1, 1}, {-1, -1},
		{1, 0}, {-1, 0}, {0, 1}, {0, -1}
	};

	private AttackedSquares() {
	}

	public static boolean isSquareAttacked(Board board, int squareIndex, PieceColor attackerColor) {
		return getAttackedSquares(board, attackerColor).contains(squareIndex);
	}

	public static boolean isKingInCheck(Board board, PieceColor kingColor) {
		int kingSquare = board.findKing(kingColor);
		return isSquareAttacked(board, kingSquare, kingColor.opponent());
	}

	public static Set<Integer> getAttackedSquares(Board board, PieceColor attackerColor) {
		Set<Integer> attackedSquares = new HashSet<>();

		for (int squareIndex = 0; squareIndex < SquareUtils.SQUARE_COUNT; squareIndex++) {
			Piece piece = board.get(squareIndex);
			if (piece == null || piece.color() != attackerColor) {
				continue;
			}

			int rank = SquareUtils.rankOf(squareIndex);
			int file = SquareUtils.fileOf(squareIndex);

			switch (piece.type()) {
				case PAWN -> addPawnAttacks(rank, file, attackerColor, attackedSquares);
				case KNIGHT -> addOffsetAttacks(rank, file, attackedSquares, KNIGHT_OFFSETS);
				case BISHOP -> addSlidingAttacks(board, squareIndex, attackedSquares, BISHOP_DIRECTIONS);
				case ROOK -> addSlidingAttacks(board, squareIndex, attackedSquares, ROOK_DIRECTIONS);
				case QUEEN -> addSlidingAttacks(board, squareIndex, attackedSquares, QUEEN_DIRECTIONS);
				case KING -> addOffsetAttacks(rank, file, attackedSquares, KING_OFFSETS);
			}
		}

		return attackedSquares;
	}

	private static void addPawnAttacks(int rank, int file, PieceColor color, Set<Integer> attackedSquares) {
		int forward = color == PieceColor.WHITE ? 1 : -1;
		for (int fileOffset : new int[]{-1, 1}) {
			int targetRank = rank + forward;
			int targetFile = file + fileOffset;
			if (SquareUtils.isValidSquare(targetRank, targetFile)) {
				attackedSquares.add(SquareUtils.toIndex(targetRank, targetFile));
			}
		}
	}

	private static void addOffsetAttacks(int rank, int file, Set<Integer> attackedSquares, int[][] offsets) {
		for (int[] offset : offsets) {
			int targetRank = rank + offset[0];
			int targetFile = file + offset[1];
			if (SquareUtils.isValidSquare(targetRank, targetFile)) {
				attackedSquares.add(SquareUtils.toIndex(targetRank, targetFile));
			}
		}
	}

	private static void addSlidingAttacks(Board board, int squareIndex, Set<Integer> attackedSquares, int[][] directions) {
		int startRank = SquareUtils.rankOf(squareIndex);
		int startFile = SquareUtils.fileOf(squareIndex);

		for (int[] direction : directions) {
			int targetRank = startRank + direction[0];
			int targetFile = startFile + direction[1];

			while (SquareUtils.isValidSquare(targetRank, targetFile)) {
				int targetSquare = SquareUtils.toIndex(targetRank, targetFile);
				attackedSquares.add(targetSquare);
				if (board.get(targetSquare) != null) {
					break;
				}
				targetRank += direction[0];
				targetFile += direction[1];
			}
		}
	}
}
