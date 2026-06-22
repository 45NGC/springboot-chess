package com.angularchess.backend.chess.board;

public final class SquareUtils {

	public static final int BOARD_SIZE = 8;
	public static final int SQUARE_COUNT = BOARD_SIZE * BOARD_SIZE;

	public static final int A1 = toIndex(0, 0);
	public static final int D1 = toIndex(0, 3);
	public static final int F1 = toIndex(0, 5);
	public static final int H1 = toIndex(0, 7);
	public static final int A8 = toIndex(7, 0);
	public static final int D8 = toIndex(7, 3);
	public static final int F8 = toIndex(7, 5);
	public static final int H8 = toIndex(7, 7);

	private SquareUtils() {
	}

	public static int toIndex(int rank, int file) {
		return rank * BOARD_SIZE + file;
	}

	public static int rankOf(int square) {
		return square / BOARD_SIZE;
	}

	public static int fileOf(int square) {
		return square % BOARD_SIZE;
	}

	public static boolean isValidSquare(int rank, int file) {
		return rank >= 0 && rank < BOARD_SIZE && file >= 0 && file < BOARD_SIZE;
	}
}
