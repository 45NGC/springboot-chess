package com.angularchess.backend.chess.board;

import java.util.Arrays;

import com.angularchess.backend.chess.model.Piece;
import com.angularchess.backend.chess.model.PieceColor;
import com.angularchess.backend.chess.model.PieceType;
import com.angularchess.backend.online.model.Move;

public class Board {

	private final Piece[] squares;
	private Integer enPassantTarget;
	private CastlingAvailability castlingRights = new CastlingAvailability();

	public Board() {
		this.squares = new Piece[SquareUtils.SQUARE_COUNT];
	}

	public Board(Piece[] initial) {
		this.squares = Arrays.copyOf(initial, initial.length);
	}

	public Piece get(int square) {
		return squares[square];
	}

	public void set(int square, Piece piece) {
		squares[square] = piece;
	}

	public Piece[] getSquares() {
		return squares;
	}

	public Integer getEnPassantTarget() {
		return enPassantTarget;
	}

	public void setEnPassantTarget(Integer enPassantTarget) {
		this.enPassantTarget = enPassantTarget;
	}

	public CastlingAvailability getCastlingRights() {
		return castlingRights;
	}

	public void setCastlingRights(CastlingAvailability castlingRights) {
		this.castlingRights = castlingRights;
	}

	public Board copy() {
		Board copy = new Board(squares);
		copy.setEnPassantTarget(enPassantTarget);
		copy.setCastlingRights(castlingRights.copy());
		return copy;
	}

	public int findKing(PieceColor color) {
		for (int square = 0; square < squares.length; square++) {
			Piece piece = squares[square];
			if (piece != null && piece.type() == PieceType.KING && piece.color() == color) {
				return square;
			}
		}
		throw new IllegalStateException("King of color " + color + " not found on board");
	}

	public void updateCastlingRights(Move move, Piece piece, Piece capturedPiece) {
		int from = move.from();

		if (piece.type() == PieceType.KING) {
			if (piece.color() == PieceColor.WHITE) {
				castlingRights.getWhite().setShortCastle(false);
				castlingRights.getWhite().setLongCastle(false);
			} else {
				castlingRights.getBlack().setShortCastle(false);
				castlingRights.getBlack().setLongCastle(false);
			}
		}

		if (piece.type() == PieceType.ROOK) {
			disableRookRightsForOrigin(piece.color(), from);
		}

		if (capturedPiece != null && capturedPiece.type() == PieceType.ROOK) {
			disableRookRightsForOrigin(capturedPiece.color(), move.to());
		}
	}

	private void disableRookRightsForOrigin(PieceColor color, int square) {
		if (color == PieceColor.WHITE) {
			if (square == SquareUtils.A1) {
				castlingRights.getWhite().setLongCastle(false);
			} else if (square == SquareUtils.H1) {
				castlingRights.getWhite().setShortCastle(false);
			}
		} else {
			if (square == SquareUtils.A8) {
				castlingRights.getBlack().setLongCastle(false);
			} else if (square == SquareUtils.H8) {
				castlingRights.getBlack().setShortCastle(false);
			}
		}
	}
}
