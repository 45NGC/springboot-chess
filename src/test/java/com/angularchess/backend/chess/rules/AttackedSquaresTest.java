package com.angularchess.backend.chess.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.angularchess.backend.chess.TestBoards;
import com.angularchess.backend.chess.board.Board;
import com.angularchess.backend.chess.board.SquareUtils;
import com.angularchess.backend.chess.model.PieceColor;
import com.angularchess.backend.chess.model.PieceType;

class AttackedSquaresTest {

	@Test
	void whitePawnAttacksDiagonallyForward() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . . . . .
		 * 7| . . . . . . . .
		 * 6| . . . . . . . .
		 * 5| . . . P . . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . . . . .
		 * 1| . . . . . . . .
		 *  -----------------
		 * White pawn on d4 attacks c5 and e5
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(3, 3), TestBoards.white(PieceType.PAWN));

		var attacked = AttackedSquares.getAttackedSquares(board, PieceColor.WHITE);

		assertTrue(attacked.contains(SquareUtils.toIndex(4, 2)));
		assertTrue(attacked.contains(SquareUtils.toIndex(4, 4)));
	}

	@Test
	void rookAttacksStopAtFirstBlocker() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . . . . .
		 * 7| . . . . . . . .
		 * 6| . . . . . . . .
		 * 5| . . . . R . P .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . . . . .
		 * 1| . . . . . . . .
		 *  -----------------
		 * White rook on e5 attacks through rank until blocked by own pawn on g5
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(4, 4), TestBoards.white(PieceType.ROOK));
		board.set(SquareUtils.toIndex(4, 6), TestBoards.white(PieceType.PAWN));

		var attacked = AttackedSquares.getAttackedSquares(board, PieceColor.WHITE);

		assertTrue(attacked.contains(SquareUtils.toIndex(4, 6)));
		assertFalse(attacked.contains(SquareUtils.toIndex(4, 7)));
	}

	@Test
	void isSquareAttackedReturnsFalseWhenTargetIsSafe() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . . . . .
		 * 7| . . . . . . . .
		 * 6| . . . . . . . .
		 * 5| . . . . N . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . . . . .
		 * 1| . . . . . . . .
		 *  -----------------
		 * White knight on e5 does not attack a1
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(4, 4), TestBoards.white(PieceType.KNIGHT));

		assertFalse(AttackedSquares.isSquareAttacked(board, SquareUtils.toIndex(0, 0), PieceColor.WHITE));
	}
}
