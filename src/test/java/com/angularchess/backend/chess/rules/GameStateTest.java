package com.angularchess.backend.chess.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.angularchess.backend.chess.TestBoards;
import com.angularchess.backend.chess.board.Board;
import com.angularchess.backend.chess.board.SquareUtils;
import com.angularchess.backend.chess.model.DrawReason;
import com.angularchess.backend.chess.model.GameResultType;
import com.angularchess.backend.chess.model.PieceColor;
import com.angularchess.backend.chess.model.PieceType;
import com.angularchess.backend.online.model.Move;

class GameStateTest {

	@Test
	void detectsCheckmate() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| k . . . . . . .
		 * 7| . Q . . . . . .
		 * 6| . . K . . . . .
		 * 5| . . . . . . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . . . . .
		 * 1| . . . . . . . .
		 *  -----------------
		 * Black to move: checkmate
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(7, 0), TestBoards.black(PieceType.KING));
		board.set(SquareUtils.toIndex(6, 1), TestBoards.white(PieceType.QUEEN));
		board.set(SquareUtils.toIndex(5, 2), TestBoards.white(PieceType.KING));

		GameState state = new GameState(board);
		state.setTurn(PieceColor.BLACK);
		recompute(state);

		assertEquals(GameResultType.CHECKMATE, state.getResult().type());
		assertEquals(PieceColor.WHITE, state.getResult().winner());
	}

	@Test
	void detectsStalemate() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| k . . . . . . .
		 * 7| . . . . . . . .
		 * 6| . Q K . . . . .
		 * 5| . . . . . . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| . . . . . . . .
		 * 1| . . . . . . . .
		 *  -----------------
		 * Black to move: stalemate
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(7, 0), TestBoards.black(PieceType.KING));
		board.set(SquareUtils.toIndex(5, 1), TestBoards.white(PieceType.QUEEN));
		board.set(SquareUtils.toIndex(5, 2), TestBoards.white(PieceType.KING));

		GameState state = new GameState(board);
		state.setTurn(PieceColor.BLACK);
		recompute(state);

		assertEquals(GameResultType.STALEMATE, state.getResult().type());
	}

	@Test
	void detectsInsufficientMaterial() {
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
		 * 1| . . B . K . . .
		 *  -----------------
		 * K+B vs K: draw by insufficient material
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(0, 4), TestBoards.white(PieceType.KING));
		board.set(SquareUtils.toIndex(7, 4), TestBoards.black(PieceType.KING));
		board.set(SquareUtils.toIndex(0, 2), TestBoards.white(PieceType.BISHOP));

		GameState state = new GameState(board);
		recompute(state);

		assertEquals(GameResultType.DRAW, state.getResult().type());
		assertEquals(DrawReason.INSUFFICIENT_MATERIAL, state.getResult().drawReason());
	}

	@Test
	void detectsThreefoldRepetition() {
		/*
		 *    a b c d e f g h
		 *  -----------------
		 * 8| . . . . k . . .
		 * 7| r . . . . . . .
		 * 6| . . . . . . . .
		 * 5| . . . . . . . .
		 * 4| . . . . . . . .
		 * 3| . . . . . . . .
		 * 2| R . . . . . . .
		 * 1| . . . . K . . .
		 *  -----------------
		 * Both rooks shuffle on file a until the same position appears three times
		 */
		Board board = TestBoards.emptyBoard();
		board.set(SquareUtils.toIndex(0, 4), TestBoards.white(PieceType.KING));
		board.set(SquareUtils.toIndex(7, 4), TestBoards.black(PieceType.KING));
		board.set(SquareUtils.toIndex(1, 0), TestBoards.white(PieceType.ROOK));
		board.set(SquareUtils.toIndex(6, 0), TestBoards.black(PieceType.ROOK));
		board.getCastlingRights().getWhite().setShortCastle(false);
		board.getCastlingRights().getWhite().setLongCastle(false);
		board.getCastlingRights().getBlack().setShortCastle(false);
		board.getCastlingRights().getBlack().setLongCastle(false);

		GameState state = new GameState(board);
		state.applyMove(new Move(SquareUtils.toIndex(1, 0), SquareUtils.toIndex(2, 0), null, null, null, null));
		state.applyMove(new Move(SquareUtils.toIndex(6, 0), SquareUtils.toIndex(5, 0), null, null, null, null));
		state.applyMove(new Move(SquareUtils.toIndex(2, 0), SquareUtils.toIndex(1, 0), null, null, null, null));
		state.applyMove(new Move(SquareUtils.toIndex(5, 0), SquareUtils.toIndex(6, 0), null, null, null, null));
		state.applyMove(new Move(SquareUtils.toIndex(1, 0), SquareUtils.toIndex(2, 0), null, null, null, null));
		state.applyMove(new Move(SquareUtils.toIndex(6, 0), SquareUtils.toIndex(5, 0), null, null, null, null));
		state.applyMove(new Move(SquareUtils.toIndex(2, 0), SquareUtils.toIndex(1, 0), null, null, null, null));
		state.applyMove(new Move(SquareUtils.toIndex(5, 0), SquareUtils.toIndex(6, 0), null, null, null, null));

		assertEquals(GameResultType.DRAW, state.getResult().type());
		assertEquals(DrawReason.THREEFOLD_REPETITION, state.getResult().drawReason());
	}

	private void recompute(GameState state) {
		try {
			Method method = GameState.class.getDeclaredMethod("updateGameResult");
			method.setAccessible(true);
			method.invoke(state);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
			throw new RuntimeException(exception);
		}
	}
}
