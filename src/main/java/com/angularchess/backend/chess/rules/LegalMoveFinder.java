package com.angularchess.backend.chess.rules;

import java.util.ArrayList;
import java.util.List;

import com.angularchess.backend.chess.board.Board;
import com.angularchess.backend.chess.board.CastlingRights;
import com.angularchess.backend.chess.board.SquareUtils;
import com.angularchess.backend.chess.model.Piece;
import com.angularchess.backend.chess.model.PieceColor;
import com.angularchess.backend.chess.model.PieceType;
import com.angularchess.backend.online.model.CastlingType;
import com.angularchess.backend.online.model.Move;
import com.angularchess.backend.online.model.PromotionPiece;

public class LegalMoveFinder {

	private static final int WHITE_BACK_RANK = 0;
	private static final int BLACK_BACK_RANK = 7;
	private static final int WHITE_PAWN_INITIAL_RANK = 1;
	private static final int BLACK_PAWN_INITIAL_RANK = 6;
	private static final int WHITE_PROMOTION_RANK = 7;
	private static final int BLACK_PROMOTION_RANK = 0;

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

	public List<Move> getLegalMoves(Board board, int square) {
		Piece piece = board.get(square);
		if (piece == null) {
			return List.of();
		}

		List<Move> pseudoLegalMoves = generatePseudoLegalMoves(board, square, piece.color());
		List<Move> legalMoves = new ArrayList<>();

		for (Move move : pseudoLegalMoves) {
			Board nextBoard = MoveSimulator.simulate(board, move);
			if (!AttackedSquares.isKingInCheck(nextBoard, piece.color())) {
				legalMoves.add(move);
			}
		}

		return legalMoves;
	}

	private List<Move> generatePseudoLegalMoves(Board board, int square, PieceColor color) {
		Piece piece = board.get(square);
		if (piece == null) {
			return List.of();
		}

		return switch (piece.type()) {
			case KNIGHT -> knightMoves(board, square, color);
			case KING -> kingMoves(board, square, color);
			case PAWN -> pawnMoves(board, square, color);
			case ROOK -> slidingMoves(board, square, color, ROOK_DIRECTIONS);
			case BISHOP -> slidingMoves(board, square, color, BISHOP_DIRECTIONS);
			case QUEEN -> slidingMoves(board, square, color, QUEEN_DIRECTIONS);
		};
	}

	private List<Move> knightMoves(Board board, int square, PieceColor color) {
		int rank = SquareUtils.rankOf(square);
		int file = SquareUtils.fileOf(square);
		List<Move> moves = new ArrayList<>();

		for (int[] offset : KNIGHT_OFFSETS) {
			int targetRank = rank + offset[0];
			int targetFile = file + offset[1];
			if (!SquareUtils.isValidSquare(targetRank, targetFile)) {
				continue;
			}

			int targetSquare = SquareUtils.toIndex(targetRank, targetFile);
			Piece target = board.get(targetSquare);
			if (target == null || target.color() != color) {
				moves.add(new Move(square, targetSquare, null, null, null, null));
			}
		}

		return moves;
	}

	private List<Move> kingMoves(Board board, int square, PieceColor color) {
		int rank = SquareUtils.rankOf(square);
		int file = SquareUtils.fileOf(square);
		List<Move> moves = new ArrayList<>();

		for (int[] offset : KING_OFFSETS) {
			int targetRank = rank + offset[0];
			int targetFile = file + offset[1];
			if (!SquareUtils.isValidSquare(targetRank, targetFile)) {
				continue;
			}

			int targetSquare = SquareUtils.toIndex(targetRank, targetFile);
			Piece target = board.get(targetSquare);
			if (target == null || target.color() != color) {
				moves.add(new Move(square, targetSquare, null, null, null, null));
			}
		}

		addCastlingMoves(board, square, color, moves);
		return moves;
	}

	private void addCastlingMoves(Board board, int kingSquare, PieceColor color, List<Move> moves) {
		if (AttackedSquares.isKingInCheck(board, color)) {
			return;
		}

		CastlingRights rights = color == PieceColor.WHITE
			? board.getCastlingRights().getWhite()
			: board.getCastlingRights().getBlack();
		int homeRank = color == PieceColor.WHITE ? WHITE_BACK_RANK : BLACK_BACK_RANK;

		if (rights.isShortCastle()) {
			int rookSquare = SquareUtils.toIndex(homeRank, 7);
			Piece rook = board.get(rookSquare);
			boolean squaresEmpty = board.get(SquareUtils.toIndex(homeRank, 5)) == null
				&& board.get(SquareUtils.toIndex(homeRank, 6)) == null;
			boolean squaresSafe = !AttackedSquares.isSquareAttacked(board, SquareUtils.toIndex(homeRank, 5), color.opponent())
				&& !AttackedSquares.isSquareAttacked(board, SquareUtils.toIndex(homeRank, 6), color.opponent());

			if (rook != null && rook.type() == PieceType.ROOK && rook.color() == color && squaresEmpty && squaresSafe) {
				moves.add(new Move(kingSquare, SquareUtils.toIndex(homeRank, 6), null, null, CastlingType.KING_SIDE, null));
			}
		}

		if (rights.isLongCastle()) {
			int rookSquare = SquareUtils.toIndex(homeRank, 0);
			Piece rook = board.get(rookSquare);
			boolean squaresEmpty = board.get(SquareUtils.toIndex(homeRank, 1)) == null
				&& board.get(SquareUtils.toIndex(homeRank, 2)) == null
				&& board.get(SquareUtils.toIndex(homeRank, 3)) == null;
			boolean squaresSafe = !AttackedSquares.isSquareAttacked(board, SquareUtils.toIndex(homeRank, 2), color.opponent())
				&& !AttackedSquares.isSquareAttacked(board, SquareUtils.toIndex(homeRank, 3), color.opponent());

			if (rook != null && rook.type() == PieceType.ROOK && rook.color() == color && squaresEmpty && squaresSafe) {
				moves.add(new Move(kingSquare, SquareUtils.toIndex(homeRank, 2), null, null, CastlingType.QUEEN_SIDE, null));
			}
		}
	}

	private List<Move> pawnMoves(Board board, int square, PieceColor color) {
		int rank = SquareUtils.rankOf(square);
		int file = SquareUtils.fileOf(square);
		List<Move> moves = new ArrayList<>();
		int forward = color == PieceColor.WHITE ? 1 : -1;
		int nextRank = rank + forward;

		if (!SquareUtils.isValidSquare(nextRank, file)) {
			return moves;
		}

		int oneStepSquare = SquareUtils.toIndex(nextRank, file);
		int promotionRank = color == PieceColor.WHITE ? WHITE_PROMOTION_RANK : BLACK_PROMOTION_RANK;
		boolean isPromotionRank = nextRank == promotionRank;

		if (board.get(oneStepSquare) == null) {
			if (isPromotionRank) {
				addPromotions(moves, square, oneStepSquare);
			} else {
				moves.add(new Move(square, oneStepSquare, null, null, null, null));
			}
		}

		int initialRank = color == PieceColor.WHITE ? WHITE_PAWN_INITIAL_RANK : BLACK_PAWN_INITIAL_RANK;
		if (rank == initialRank && board.get(oneStepSquare) == null) {
			int twoStepSquare = SquareUtils.toIndex(rank + (2 * forward), file);
			if (board.get(twoStepSquare) == null) {
				moves.add(new Move(square, twoStepSquare, null, null, null, true));
			}
		}

		for (int targetFile : new int[]{file - 1, file + 1}) {
			if (!SquareUtils.isValidSquare(nextRank, targetFile)) {
				continue;
			}

			int targetSquare = SquareUtils.toIndex(nextRank, targetFile);
			Piece target = board.get(targetSquare);
			if (target != null && target.color() != color) {
				if (isPromotionRank) {
					addPromotions(moves, square, targetSquare);
				} else {
					moves.add(new Move(square, targetSquare, null, null, null, null));
				}
			}
		}

		if (board.getEnPassantTarget() != null) {
			int enPassantRank = color == PieceColor.WHITE ? 4 : 3;
			if (rank == enPassantRank) {
				int enPassantFile = SquareUtils.fileOf(board.getEnPassantTarget());
				if (Math.abs(file - enPassantFile) == 1) {
					int captureSquare = SquareUtils.toIndex(nextRank, enPassantFile);
					moves.add(new Move(square, captureSquare, null, true, null, null));
				}
			}
		}

		return moves;
	}

	private void addPromotions(List<Move> moves, int from, int to) {
		for (PromotionPiece promotion : PromotionPiece.values()) {
			moves.add(new Move(from, to, promotion, null, null, null));
		}
	}

	private List<Move> slidingMoves(Board board, int square, PieceColor color, int[][] directions) {
		int rank = SquareUtils.rankOf(square);
		int file = SquareUtils.fileOf(square);
		List<Move> moves = new ArrayList<>();

		for (int[] direction : directions) {
			int targetRank = rank + direction[0];
			int targetFile = file + direction[1];

			while (SquareUtils.isValidSquare(targetRank, targetFile)) {
				int targetSquare = SquareUtils.toIndex(targetRank, targetFile);
				Piece target = board.get(targetSquare);

				if (target == null) {
					moves.add(new Move(square, targetSquare, null, null, null, null));
				} else {
					if (target.color() != color) {
						moves.add(new Move(square, targetSquare, null, null, null, null));
					}
					break;
				}

				targetRank += direction[0];
				targetFile += direction[1];
			}
		}

		return moves;
	}
}
