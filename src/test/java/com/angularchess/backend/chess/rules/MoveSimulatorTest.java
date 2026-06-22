package com.angularchess.backend.chess.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.angularchess.backend.chess.TestBoards;
import com.angularchess.backend.chess.board.Board;
import com.angularchess.backend.chess.board.SquareUtils;
import com.angularchess.backend.chess.model.PieceType;
import com.angularchess.backend.online.model.Move;
import com.angularchess.backend.online.model.PromotionPiece;

class MoveSimulatorTest {

	@Test
	void movingFromEmptySquareThrows() {
		/*
		 * Empty board:
		 * trying to move from square 30 should fail
		 */
		Board board = TestBoards.emptyBoard();

		IllegalArgumentException error = assertThrows(
			IllegalArgumentException.class,
			() -> MoveSimulator.simulate(board, new Move(30, 31, null, null, null, null))
		);

		assertEquals("Cannot simulate move: no piece at 30", error.getMessage());
	}

	@Test
	void promotionReplacesPawnWithRequestedPiece() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . . . . .
		 * 7| P . . . . . . .
		 * 6| . . . . . . . .
		 * 5| . . . . . . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . . . . .
		 * 1| . . . . . . . .
		 *  -----------------
		 * a7 -> a8 =Q
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(6, 0), TestBoards.white(PieceType.PAWN));

		Board result = MoveSimulator.simulate(
			board,
			new Move(SquareUtils.toIndex(6, 0), SquareUtils.toIndex(7, 0), PromotionPiece.QUEEN, null, null, null)
		);

		assertEquals(TestBoards.white(PieceType.QUEEN), result.get(SquareUtils.toIndex(7, 0)));
		assertNull(result.get(SquareUtils.toIndex(6, 0)));
	}

	@Test
	void enPassantRemovesTheCapturedPawn() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . . . . .
		 * 7| . . . . . . . .
		 * 6| . . . . . . . .
		 * 5| . . . p P . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . . . . .
		 * 1| . . . . . . . .
		 *  -----------------
		 * e5 x d6 en passant
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(4, 4), TestBoards.white(PieceType.PAWN));
		board.set(SquareUtils.toIndex(4, 3), TestBoards.black(PieceType.PAWN));
		board.setEnPassantTarget(SquareUtils.toIndex(5, 3));

		Board result = MoveSimulator.simulate(
			board,
			new Move(SquareUtils.toIndex(4, 4), SquareUtils.toIndex(5, 3), null, true, null, null)
		);

		assertEquals(TestBoards.white(PieceType.PAWN), result.get(SquareUtils.toIndex(5, 3)));
		assertNull(result.get(SquareUtils.toIndex(4, 3)));
		assertNull(result.get(SquareUtils.toIndex(4, 4)));
	}

	@Test
	void doublePawnPushSetsEnPassantTarget() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . . . . .
		 * 7| . . . . . . . .
		 * 6| . . . . . . . .
		 * 5| . . . . . . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . P . . .
		 * 1| . . . . . . . .
		 *  -----------------
		 * e2 -> e4 sets en passant target on e3
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(1, 4), TestBoards.white(PieceType.PAWN));

		Board result = MoveSimulator.simulate(
			board,
			new Move(SquareUtils.toIndex(1, 4), SquareUtils.toIndex(3, 4), null, null, null, null)
		);

		assertEquals(SquareUtils.toIndex(2, 4), result.getEnPassantTarget());
	}
}
