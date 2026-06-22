package com.angularchess.backend.chess.board;

import java.util.HashMap;
import java.util.Map;

import com.angularchess.backend.chess.model.Piece;
import com.angularchess.backend.chess.model.PieceColor;
import com.angularchess.backend.chess.model.PieceType;

public final class Fen {

	public static final String INITIAL_POSITION_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

	private static final Map<Character, Piece> FEN_TO_PIECE = new HashMap<>();
	private static final Map<PieceType, Map<PieceColor, Character>> PIECE_TO_FEN = new HashMap<>();

	static {
		FEN_TO_PIECE.put('p', new Piece(PieceType.PAWN, PieceColor.BLACK));
		FEN_TO_PIECE.put('n', new Piece(PieceType.KNIGHT, PieceColor.BLACK));
		FEN_TO_PIECE.put('b', new Piece(PieceType.BISHOP, PieceColor.BLACK));
		FEN_TO_PIECE.put('r', new Piece(PieceType.ROOK, PieceColor.BLACK));
		FEN_TO_PIECE.put('q', new Piece(PieceType.QUEEN, PieceColor.BLACK));
		FEN_TO_PIECE.put('k', new Piece(PieceType.KING, PieceColor.BLACK));
		FEN_TO_PIECE.put('P', new Piece(PieceType.PAWN, PieceColor.WHITE));
		FEN_TO_PIECE.put('N', new Piece(PieceType.KNIGHT, PieceColor.WHITE));
		FEN_TO_PIECE.put('B', new Piece(PieceType.BISHOP, PieceColor.WHITE));
		FEN_TO_PIECE.put('R', new Piece(PieceType.ROOK, PieceColor.WHITE));
		FEN_TO_PIECE.put('Q', new Piece(PieceType.QUEEN, PieceColor.WHITE));
		FEN_TO_PIECE.put('K', new Piece(PieceType.KING, PieceColor.WHITE));

		putPieceFen(PieceType.PAWN, 'P', 'p');
		putPieceFen(PieceType.KNIGHT, 'N', 'n');
		putPieceFen(PieceType.BISHOP, 'B', 'b');
		putPieceFen(PieceType.ROOK, 'R', 'r');
		putPieceFen(PieceType.QUEEN, 'Q', 'q');
		putPieceFen(PieceType.KING, 'K', 'k');
	}

	private Fen() {
	}

	public static void load(Board board, String fen) {
		String[] fields = fen.split(" ");
		String[] ranks = fields[0].split("/");
		if (ranks.length != 8) {
			throw new IllegalArgumentException("Invalid FEN: expected 8 ranks, got " + ranks.length);
		}

		for (int fenRank = 0; fenRank < 8; fenRank++) {
			String rankString = ranks[fenRank];
			int file = 0;

			for (int index = 0; index < rankString.length(); index++) {
				char symbol = rankString.charAt(index);
				if (Character.isDigit(symbol)) {
					file += Character.digit(symbol, 10);
					continue;
				}

				Piece piece = FEN_TO_PIECE.get(symbol);
				if (piece == null) {
					throw new IllegalArgumentException("Invalid FEN piece char: " + symbol);
				}

				int rank = 7 - fenRank;
				board.set(SquareUtils.toIndex(rank, file), piece);
				file++;
			}

			if (file != 8) {
				throw new IllegalArgumentException("Invalid FEN rank: " + rankString);
			}
		}
	}

	public static String boardToFen(Board board) {
		StringBuilder result = new StringBuilder();

		for (int rank = 7; rank >= 0; rank--) {
			int emptyCount = 0;

			for (int file = 0; file < 8; file++) {
				Piece piece = board.get(SquareUtils.toIndex(rank, file));
				if (piece == null) {
					emptyCount++;
					continue;
				}

				if (emptyCount > 0) {
					result.append(emptyCount);
					emptyCount = 0;
				}

				result.append(PIECE_TO_FEN.get(piece.type()).get(piece.color()));
			}

			if (emptyCount > 0) {
				result.append(emptyCount);
			}

			if (rank > 0) {
				result.append('/');
			}
		}

		return result.toString();
	}

	public static String toFen(Board board, PieceColor turn) {
		String placement = boardToFen(board);
		String active = turn == PieceColor.WHITE ? "w" : "b";
		String castling = castlingToFen(board);
		String enPassant = board.getEnPassantTarget() != null ? indexToAlgebraic(board.getEnPassantTarget()) : "-";
		return placement + " " + active + " " + castling + " " + enPassant + " 0 1";
	}

	private static void putPieceFen(PieceType type, char white, char black) {
		Map<PieceColor, Character> mapping = new HashMap<>();
		mapping.put(PieceColor.WHITE, white);
		mapping.put(PieceColor.BLACK, black);
		PIECE_TO_FEN.put(type, mapping);
	}

	private static String castlingToFen(Board board) {
		StringBuilder rights = new StringBuilder();
		if (board.getCastlingRights().getWhite().isShortCastle()) {
			rights.append('K');
		}
		if (board.getCastlingRights().getWhite().isLongCastle()) {
			rights.append('Q');
		}
		if (board.getCastlingRights().getBlack().isShortCastle()) {
			rights.append('k');
		}
		if (board.getCastlingRights().getBlack().isLongCastle()) {
			rights.append('q');
		}
		return rights.isEmpty() ? "-" : rights.toString();
	}

	private static String indexToAlgebraic(int index) {
		int file = SquareUtils.fileOf(index);
		int rank = SquareUtils.rankOf(index);
		return String.valueOf((char) ('a' + file)) + (rank + 1);
	}
}
