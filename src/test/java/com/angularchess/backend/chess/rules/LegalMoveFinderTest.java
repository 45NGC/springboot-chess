package com.angularchess.backend.chess.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.angularchess.backend.chess.TestBoards;
import com.angularchess.backend.chess.board.Board;
import com.angularchess.backend.chess.board.SquareUtils;
import com.angularchess.backend.chess.model.PieceType;
import com.angularchess.backend.online.model.CastlingType;
import com.angularchess.backend.online.model.Move;
import com.angularchess.backend.online.model.PromotionPiece;

class LegalMoveFinderTest {

	private LegalMoveFinder moveFinder;

	@BeforeEach
	void setUp() {
		moveFinder = new LegalMoveFinder();
	}

	@Test
	void emptySquareReturnsNoMoves() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . k . . .
		 * 7| . . . . . . . .
		 * 6| . . . . . . . .
		 * 5| . . . . . . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . . . . .
		 * 1| . . . . K . . .
		 *  -----------------
		 * Asking for moves from e5 on an empty square returns no moves
		 */
		Board board = TestBoards.emptyBoard();
		TestBoards.addKings(board);

		assertTrue(moveFinder.getLegalMoves(board, SquareUtils.toIndex(4, 4)).isEmpty());
	}

	@Test
	void knightGetsAllEightMovesOnEmptyBoard() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . k . . .
		 * 7| . . . . . . . .
		 * 6| . . . . . . . .
		 * 5| . . . . N . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . . . . .
		 * 1| . . . . K . . .
		 *  -----------------
		 * White knight on e5 has 8 legal moves
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(4, 4), TestBoards.white(PieceType.KNIGHT));
		TestBoards.addKings(board);

		List<Move> moves = moveFinder.getLegalMoves(board, SquareUtils.toIndex(4, 4));

		assertEquals(8, moves.size());
		assertTrue(moves.contains(new Move(SquareUtils.toIndex(4, 4), SquareUtils.toIndex(6, 5), null, null, null, null)));
		assertTrue(moves.contains(new Move(SquareUtils.toIndex(4, 4), SquareUtils.toIndex(3, 2), null, null, null, null)));
	}

	@Test
	void kingSideCastlingIsGeneratedWhenConditionsAreMet() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . k . . .
		 * 7| . . . . . . . .
		 * 6| . . . . . . . .
		 * 5| . . . . . . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . . . . .
		 * 1| . . . . K . . R
		 *  -----------------
		 * White can castle king side: e1 -> g1
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(0, 4), TestBoards.white(PieceType.KING));
		board.set(SquareUtils.toIndex(0, 7), TestBoards.white(PieceType.ROOK));
		board.set(SquareUtils.toIndex(7, 4), TestBoards.black(PieceType.KING));

		List<Move> moves = moveFinder.getLegalMoves(board, SquareUtils.toIndex(0, 4));

		assertTrue(moves.contains(new Move(
			SquareUtils.toIndex(0, 4),
			SquareUtils.toIndex(0, 6),
			null,
			null,
			CastlingType.KING_SIDE,
			null
		)));
	}

	@Test
	void castlingIsRejectedThroughCheck() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . k . . .
		 * 7| . . . . . . . .
		 * 6| . . . . . . . .
		 * 5| . . . . . . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . . r . .
		 * 1| . . . . K . . R
		 *  -----------------
		 * Black rook attacks f1, so white cannot castle through check
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(0, 4), TestBoards.white(PieceType.KING));
		board.set(SquareUtils.toIndex(0, 7), TestBoards.white(PieceType.ROOK));
		board.set(SquareUtils.toIndex(1, 5), TestBoards.black(PieceType.ROOK));
		board.set(SquareUtils.toIndex(7, 4), TestBoards.black(PieceType.KING));

		List<Move> moves = moveFinder.getLegalMoves(board, SquareUtils.toIndex(0, 4));

		assertFalse(moves.stream().anyMatch(move -> move.castling() == CastlingType.KING_SIDE));
	}

	@Test
	void pinnedPieceCannotExposeOwnKing() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . . . . k
		 * 7| . . . . r . . .
		 * 6| . . . . . . . .
		 * 5| . . . . . . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . R . . .
		 * 1| . . . . K . . .
		 *  -----------------
		 * White rook on e2 is pinned by black rook on e8 and cannot move sideways
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(0, 4), TestBoards.white(PieceType.KING));
		board.set(SquareUtils.toIndex(1, 4), TestBoards.white(PieceType.ROOK));
		board.set(SquareUtils.toIndex(7, 4), TestBoards.black(PieceType.ROOK));
		board.set(SquareUtils.toIndex(7, 7), TestBoards.black(PieceType.KING));

		List<Move> moves = moveFinder.getLegalMoves(board, SquareUtils.toIndex(1, 4));

		assertFalse(moves.contains(new Move(SquareUtils.toIndex(1, 4), SquareUtils.toIndex(1, 5), null, null, null, null)));
		assertTrue(moves.contains(new Move(SquareUtils.toIndex(1, 4), SquareUtils.toIndex(2, 4), null, null, null, null)));
	}

	@Test
	void enPassantIsGeneratedForAdjacentPawn() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . k . . .
		 * 7| . . . . . . . .
		 * 6| . . . . . . . .
		 * 5| . . . . P p . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . . . . .
		 * 1| . . . . K . . .
		 *  -----------------
		 * White pawn on e5 can capture en passant on f6
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(4, 4), TestBoards.white(PieceType.PAWN));
		board.set(SquareUtils.toIndex(4, 5), TestBoards.black(PieceType.PAWN));
		TestBoards.addKings(board);
		board.setEnPassantTarget(SquareUtils.toIndex(5, 5));

		List<Move> moves = moveFinder.getLegalMoves(board, SquareUtils.toIndex(4, 4));

		assertTrue(moves.contains(new Move(
			SquareUtils.toIndex(4, 4),
			SquareUtils.toIndex(5, 5),
			null,
			true,
			null,
			null
		)));
	}

	@Test
	void promotionGeneratesAllFourChoices() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . . . . k
		 * 7| . . . P . . . .
		 * 6| . . . . . . . .
		 * 5| . . . . . . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . . . . .
		 * 1| . . . . K . . .
		 *  -----------------
		 * White pawn on d7 can promote on d8 to Q/R/B/N
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(6, 3), TestBoards.white(PieceType.PAWN));
		board.set(SquareUtils.toIndex(0, 4), TestBoards.white(PieceType.KING));
		board.set(SquareUtils.toIndex(0, 7), TestBoards.black(PieceType.KING));

		List<Move> moves = moveFinder.getLegalMoves(board, SquareUtils.toIndex(6, 3));

		assertEquals(4, moves.size());
		assertTrue(moves.contains(new Move(SquareUtils.toIndex(6, 3), SquareUtils.toIndex(7, 3), PromotionPiece.QUEEN, null, null, null)));
		assertTrue(moves.contains(new Move(SquareUtils.toIndex(6, 3), SquareUtils.toIndex(7, 3), PromotionPiece.ROOK, null, null, null)));
		assertTrue(moves.contains(new Move(SquareUtils.toIndex(6, 3), SquareUtils.toIndex(7, 3), PromotionPiece.BISHOP, null, null, null)));
		assertTrue(moves.contains(new Move(SquareUtils.toIndex(6, 3), SquareUtils.toIndex(7, 3), PromotionPiece.KNIGHT, null, null, null)));
	}
}
