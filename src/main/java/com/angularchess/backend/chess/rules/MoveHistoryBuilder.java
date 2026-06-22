package com.angularchess.backend.chess.rules;

import java.util.List;

import com.angularchess.backend.chess.board.Board;
import com.angularchess.backend.chess.board.Fen;
import com.angularchess.backend.online.model.Move;

public final class MoveHistoryBuilder {

	private MoveHistoryBuilder() {
	}

	public static GameState buildGameStateFromMoves(List<Move> moves) {
		Board board = new Board();
		Fen.load(board, Fen.INITIAL_POSITION_FEN);
		GameState state = new GameState(board);
		for (Move move : moves) {
			state.applyMove(move);
		}
		return state;
	}
}
