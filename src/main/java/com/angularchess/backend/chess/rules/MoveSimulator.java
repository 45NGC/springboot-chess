package com.angularchess.backend.chess.rules;

import com.angularchess.backend.chess.board.Board;
import com.angularchess.backend.chess.board.SquareUtils;
import com.angularchess.backend.chess.model.Piece;
import com.angularchess.backend.chess.model.PieceColor;
import com.angularchess.backend.chess.model.PieceType;
import com.angularchess.backend.online.model.CastlingType;
import com.angularchess.backend.online.model.Move;
import com.angularchess.backend.online.model.PromotionPiece;

public final class MoveSimulator {

	private MoveSimulator() {
	}

	public static Board simulate(Board board, Move move) {
		Board boardCopy = board.copy();
		Piece piece = boardCopy.get(move.from());
		if (piece == null) {
			throw new IllegalArgumentException("Cannot simulate move: no piece at " + move.from());
		}

		boardCopy.setEnPassantTarget(null);

		if (piece.type() == PieceType.PAWN) {
			handlePawnMoves(boardCopy, move, piece);
		}

		if (move.castling() != null && piece.type() == PieceType.KING) {
			handleCastling(boardCopy, move.castling(), piece.color());
		}

		Piece pieceToPlace = resolvePieceToPlace(piece, move.promotion());
		boardCopy.set(move.to(), pieceToPlace);
		boardCopy.set(move.from(), null);
		return boardCopy;
	}

	private static void handlePawnMoves(Board board, Move move, Piece piece) {
		if (Boolean.TRUE.equals(move.enPassant())) {
			int capturedPawnOffset = piece.color() == PieceColor.WHITE ? -SquareUtils.BOARD_SIZE : SquareUtils.BOARD_SIZE;
			board.set(move.to() + capturedPawnOffset, null);
		}

		int fromRank = SquareUtils.rankOf(move.from());
		int toRank = SquareUtils.rankOf(move.to());
		if (Math.abs(toRank - fromRank) == 2) {
			int passedRank = (fromRank + toRank) / 2;
			int file = SquareUtils.fileOf(move.from());
			board.setEnPassantTarget(SquareUtils.toIndex(passedRank, file));
		}
	}

	private static void handleCastling(Board board, CastlingType castlingType, PieceColor color) {
		if (castlingType == CastlingType.KING_SIDE) {
			int rookFrom = color == PieceColor.WHITE ? SquareUtils.H1 : SquareUtils.H8;
			int rookTo = color == PieceColor.WHITE ? SquareUtils.F1 : SquareUtils.F8;
			moveRookForCastling(board, rookFrom, rookTo, color);
			return;
		}

		int rookFrom = color == PieceColor.WHITE ? SquareUtils.A1 : SquareUtils.A8;
		int rookTo = color == PieceColor.WHITE ? SquareUtils.D1 : SquareUtils.D8;
		moveRookForCastling(board, rookFrom, rookTo, color);
	}

	private static void moveRookForCastling(Board board, int rookFrom, int rookTo, PieceColor kingColor) {
		Piece rook = board.get(rookFrom);
		if (rook == null || rook.type() != PieceType.ROOK || rook.color() != kingColor) {
			return;
		}
		board.set(rookFrom, null);
		board.set(rookTo, rook);
	}

	private static Piece resolvePieceToPlace(Piece original, PromotionPiece promotion) {
		if (promotion == null || original.type() != PieceType.PAWN) {
			return original;
		}

		return switch (promotion) {
			case QUEEN -> new Piece(PieceType.QUEEN, original.color());
			case ROOK -> new Piece(PieceType.ROOK, original.color());
			case BISHOP -> new Piece(PieceType.BISHOP, original.color());
			case KNIGHT -> new Piece(PieceType.KNIGHT, original.color());
		};
	}
}
