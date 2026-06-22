package com.angularchess.backend.chess.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.angularchess.backend.chess.board.Board;
import com.angularchess.backend.chess.board.Fen;
import com.angularchess.backend.chess.board.SquareUtils;
import com.angularchess.backend.chess.model.DrawReason;
import com.angularchess.backend.chess.model.GameResult;
import com.angularchess.backend.chess.model.GameResultType;
import com.angularchess.backend.chess.model.Piece;
import com.angularchess.backend.chess.model.PieceColor;
import com.angularchess.backend.online.model.Move;

public class GameState {

	private GameResult result = GameResult.ongoing();
	private Board board;
	private PieceColor turn = PieceColor.WHITE;
	private final Map<String, Integer> positionCounts = new HashMap<>();

	public GameState(Board board) {
		this.board = board;
		recordCurrentPosition();
	}

	public Board getBoard() {
		return board;
	}

	public PieceColor getTurn() {
		return turn;
	}

	public void setTurn(PieceColor turn) {
		this.turn = turn;
	}

	public GameResult getResult() {
		return result;
	}

	public void applyMove(Move move) {
		Piece piece = board.get(move.from());
		if (piece == null) {
			return;
		}

		Piece capturedPiece = board.get(move.to());
		Board nextBoard = MoveSimulator.simulate(board, move);
		nextBoard.updateCastlingRights(move, piece, capturedPiece);
		board = nextBoard;
		turn = turn.opponent();
		recordCurrentPosition();
		updateGameResult();
	}

	private void updateGameResult() {
		if (DrawRules.isInsufficientMaterial(board)) {
			result = GameResult.draw(DrawReason.INSUFFICIENT_MATERIAL);
			return;
		}

		int repetitionCount = positionCounts.getOrDefault(getPositionKey(), 1);
		if (repetitionCount >= 3) {
			result = GameResult.draw(DrawReason.THREEFOLD_REPETITION);
			return;
		}

		List<Move> legalMoves = getAllLegalMoves(turn);
		if (!legalMoves.isEmpty()) {
			result = GameResult.ongoing();
			return;
		}

		if (AttackedSquares.isKingInCheck(board, turn)) {
			result = GameResult.checkmate(turn.opponent());
		} else {
			result = GameResult.stalemate();
		}
	}

	private void recordCurrentPosition() {
		String key = getPositionKey();
		positionCounts.put(key, positionCounts.getOrDefault(key, 0) + 1);
	}

	private String getPositionKey() {
		String[] fields = Fen.toFen(board, turn).split(" ");
		return String.join(" ", fields[0], fields[1], fields[2], fields[3]);
	}

	private List<Move> getAllLegalMoves(PieceColor color) {
		List<Move> moves = new ArrayList<>();
		LegalMoveFinder moveFinder = new LegalMoveFinder();

		for (int square = 0; square < SquareUtils.SQUARE_COUNT; square++) {
			Piece piece = board.get(square);
			if (piece == null || piece.color() != color) {
				continue;
			}
			moves.addAll(moveFinder.getLegalMoves(board, square));
		}

		return moves;
	}

	public boolean isFinished() {
		return result.type() != GameResultType.ONGOING;
	}
}
