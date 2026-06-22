package com.angularchess.backend.chess.model;

public record GameResult(
	GameResultType type,
	PieceColor winner,
	DrawReason drawReason
) {
	public static GameResult ongoing() {
		return new GameResult(GameResultType.ONGOING, null, null);
	}

	public static GameResult checkmate(PieceColor winner) {
		return new GameResult(GameResultType.CHECKMATE, winner, null);
	}

	public static GameResult timeout(PieceColor winner) {
		return new GameResult(GameResultType.TIMEOUT, winner, null);
	}

	public static GameResult stalemate() {
		return new GameResult(GameResultType.STALEMATE, null, null);
	}

	public static GameResult draw(DrawReason drawReason) {
		return new GameResult(GameResultType.DRAW, null, drawReason);
	}
}
