package com.angularchess.backend.online.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.angularchess.backend.chess.model.GameResultType;
import com.angularchess.backend.chess.model.PieceColor;
import com.angularchess.backend.chess.rules.GameState;
import com.angularchess.backend.chess.rules.LegalMoveFinder;
import com.angularchess.backend.chess.rules.MoveHistoryBuilder;
import com.angularchess.backend.online.dto.CreateOnlineRoomRequest;
import com.angularchess.backend.online.dto.CreateOnlineRoomResponse;
import com.angularchess.backend.online.dto.GetOnlineRoomResponse;
import com.angularchess.backend.online.dto.JoinOnlineRoomResponse;
import com.angularchess.backend.online.dto.SubmitOnlineMoveRequest;
import com.angularchess.backend.online.dto.SubmitOnlineMoveResponse;
import com.angularchess.backend.online.model.HostSidePreference;
import com.angularchess.backend.online.model.JoinOnlineRoomError;
import com.angularchess.backend.online.model.Move;
import com.angularchess.backend.online.model.OnlineMoveRecord;
import com.angularchess.backend.online.model.OnlinePlayerPresence;
import com.angularchess.backend.online.model.OnlineRoom;
import com.angularchess.backend.online.model.OnlineRoomPlayer;
import com.angularchess.backend.online.model.OnlineRoomSession;
import com.angularchess.backend.online.model.OnlineRoomSide;
import com.angularchess.backend.online.model.OnlineRoomStatus;
import com.angularchess.backend.online.model.SubmitOnlineMoveError;
import com.angularchess.backend.online.repository.OnlineRoomRepository;
import com.angularchess.backend.online.websocket.OnlineRoomTopicPublisher;

@Service
public class InMemoryOnlineRoomService implements OnlineRoomService {

	private static final String PLAYER_ID_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
	private static final int PLAYER_ID_LENGTH = 8;
	private static final int MAX_CODE_GENERATION_ATTEMPTS = 25;

	private final OnlineRoomRepository roomRepository;
	private final OnlineRoomCodeService roomCodeService;
	private final OnlineRoomTopicPublisher roomTopicPublisher;
	private final Clock clock;
	private final SecureRandom random = new SecureRandom();
	private final LegalMoveFinder legalMoveFinder = new LegalMoveFinder();

	@Autowired
	public InMemoryOnlineRoomService(
		OnlineRoomRepository roomRepository,
		OnlineRoomCodeService roomCodeService,
		OnlineRoomTopicPublisher roomTopicPublisher
	) {
		this(roomRepository, roomCodeService, roomTopicPublisher, Clock.systemUTC());
	}

	InMemoryOnlineRoomService(
		OnlineRoomRepository roomRepository,
		OnlineRoomCodeService roomCodeService,
		OnlineRoomTopicPublisher roomTopicPublisher,
		Clock clock
	) {
		this.roomRepository = roomRepository;
		this.roomCodeService = roomCodeService;
		this.roomTopicPublisher = roomTopicPublisher;
		this.clock = clock;
	}

	@Override
	public synchronized CreateOnlineRoomResponse createRoom(CreateOnlineRoomRequest request) {
		long now = clock.millis();
		OnlineRoomSide playerSide = resolveCreatorSide(request.settings().hostSidePreference());
		OnlineRoomPlayer player = createPlayer(playerSide, now);
		String roomCode = generateUniqueRoomCode();
		OnlineRoom room = new OnlineRoom(
			roomCode,
			OnlineRoomStatus.WAITING,
			playerSide == OnlineRoomSide.WHITE ? player : null,
			playerSide == OnlineRoomSide.BLACK ? player : null,
			request.settings().timeControlSettings(),
			List.of(),
			now,
			null,
			null
		);
		OnlineRoomSession session = new OnlineRoomSession(roomCode, player.id(), playerSide);

		roomRepository.save(room);
		roomTopicPublisher.publishRoomUpdate(room);
		return new CreateOnlineRoomResponse(room, session);
	}

	@Override
	public synchronized JoinOnlineRoomResponse joinRoom(String rawCode) {
		String code = roomCodeService.normalizeCode(rawCode);
		OnlineRoom room = roomRepository.findByCode(code)
			.orElse(null);
		if (room == null) {
			return JoinOnlineRoomResponse.failure(JoinOnlineRoomError.NOT_FOUND);
		}
		if (room.status() == OnlineRoomStatus.FINISHED) {
			return JoinOnlineRoomResponse.failure(JoinOnlineRoomError.FINISHED);
		}

		OnlineRoomSide playerSide = findAvailableSide(room);
		if (playerSide == null) {
			return JoinOnlineRoomResponse.failure(JoinOnlineRoomError.FULL);
		}

		OnlineRoomPlayer player = createPlayer(playerSide, clock.millis());
		OnlineRoom updatedRoom = new OnlineRoom(
			room.code(),
			OnlineRoomStatus.READY,
			playerSide == OnlineRoomSide.WHITE ? player : room.whitePlayer(),
			playerSide == OnlineRoomSide.BLACK ? player : room.blackPlayer(),
			room.timeControlSettings(),
			room.moves(),
			room.createdAt(),
			room.startedAt(),
			room.finishedAt()
		);
		OnlineRoomSession session = new OnlineRoomSession(code, player.id(), playerSide);

		roomRepository.save(updatedRoom);
		roomTopicPublisher.publishRoomUpdate(updatedRoom);
		return JoinOnlineRoomResponse.success(updatedRoom, session);
	}

	@Override
	public GetOnlineRoomResponse getRoom(String rawCode) {
		String code = roomCodeService.normalizeCode(rawCode);
		return new GetOnlineRoomResponse(roomRepository.findByCode(code).orElse(null));
	}

	@Override
	public synchronized SubmitOnlineMoveResponse submitMove(String rawCode, SubmitOnlineMoveRequest request) {
		String code = roomCodeService.normalizeCode(rawCode);
		OnlineRoom room = roomRepository.findByCode(code)
			.orElse(null);
		if (room == null) {
			return SubmitOnlineMoveResponse.failure(SubmitOnlineMoveError.NOT_FOUND);
		}
		if (room.status() == OnlineRoomStatus.FINISHED) {
			return SubmitOnlineMoveResponse.failure(SubmitOnlineMoveError.FINISHED);
		}

		OnlineRoomPlayer player = findPlayer(room, request.playerId());
		if (player == null) {
			return SubmitOnlineMoveResponse.failure(SubmitOnlineMoveError.NOT_PARTICIPANT);
		}

		if (room.whitePlayer() == null || room.blackPlayer() == null) {
			return SubmitOnlineMoveResponse.failure(SubmitOnlineMoveError.ILLEGAL_MOVE);
		}

		GameState gameState = MoveHistoryBuilder.buildGameStateFromMoves(extractMoves(room.moves()));
		OnlineRoomSide currentTurn = toOnlineRoomSide(gameState.getTurn());
		if (currentTurn != player.side()) {
			return SubmitOnlineMoveResponse.failure(SubmitOnlineMoveError.NOT_YOUR_TURN);
		}
		Move legalMove = resolveLegalMove(gameState, request.move());
		if (legalMove == null) {
			return SubmitOnlineMoveResponse.failure(SubmitOnlineMoveError.ILLEGAL_MOVE);
		}

		long now = clock.millis();
		List<OnlineMoveRecord> updatedMoves = new ArrayList<>(room.moves());
		updatedMoves.add(new OnlineMoveRecord(copyMove(legalMove), player.side(), now));

		GameState nextState = MoveHistoryBuilder.buildGameStateFromMoves(extractMoves(updatedMoves));
		boolean finished = nextState.getResult().type() != GameResultType.ONGOING;
		OnlineRoom updatedRoom = new OnlineRoom(
			room.code(),
			finished ? OnlineRoomStatus.FINISHED : OnlineRoomStatus.PLAYING,
			room.whitePlayer(),
			room.blackPlayer(),
			room.timeControlSettings(),
			List.copyOf(updatedMoves),
			room.createdAt(),
			room.startedAt() == null ? now : room.startedAt(),
			finished ? now : null
		);

		roomRepository.save(updatedRoom);
		roomTopicPublisher.publishRoomUpdate(updatedRoom);
		return SubmitOnlineMoveResponse.success(updatedRoom);
	}

	private String generateUniqueRoomCode() {
		for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
			String code = roomCodeService.generateCode();
			if (!roomRepository.existsByCode(code)) {
				return code;
			}
		}
		throw new IllegalStateException("Could not generate a unique online room code.");
	}

	private OnlineRoomSide resolveCreatorSide(HostSidePreference hostSidePreference) {
		if (hostSidePreference == HostSidePreference.WHITE) {
			return OnlineRoomSide.WHITE;
		}
		if (hostSidePreference == HostSidePreference.BLACK) {
			return OnlineRoomSide.BLACK;
		}
		return random.nextBoolean() ? OnlineRoomSide.WHITE : OnlineRoomSide.BLACK;
	}

	private OnlineRoomPlayer createPlayer(OnlineRoomSide side, long joinedAt) {
		return new OnlineRoomPlayer(
			generatePlayerId(),
			side,
			OnlinePlayerPresence.CONNECTED,
			joinedAt
		);
	}

	private OnlineRoomPlayer findPlayer(OnlineRoom room, String playerId) {
		if (room.whitePlayer() != null && room.whitePlayer().id().equals(playerId)) {
			return room.whitePlayer();
		}
		if (room.blackPlayer() != null && room.blackPlayer().id().equals(playerId)) {
			return room.blackPlayer();
		}
		return null;
	}

	private OnlineRoomSide findAvailableSide(OnlineRoom room) {
		if (room.whitePlayer() == null) {
			return OnlineRoomSide.WHITE;
		}
		if (room.blackPlayer() == null) {
			return OnlineRoomSide.BLACK;
		}
		return null;
	}

	private Move resolveLegalMove(GameState gameState, Move move) {
		List<Move> legalMoves = legalMoveFinder.getLegalMoves(gameState.getBoard(), move.from());
		for (Move legalMove : legalMoves) {
			if (matchesClientMove(legalMove, move)) {
				return legalMove;
			}
		}
		return null;
	}

	private boolean matchesClientMove(Move legalMove, Move clientMove) {
		return legalMove.from() == clientMove.from()
			&& legalMove.to() == clientMove.to()
			&& legalMove.promotion() == clientMove.promotion();
	}

	private List<Move> extractMoves(List<OnlineMoveRecord> moveRecords) {
		return moveRecords.stream()
			.map(OnlineMoveRecord::move)
			.toList();
	}

	private OnlineRoomSide toOnlineRoomSide(PieceColor pieceColor) {
		return pieceColor == PieceColor.WHITE ? OnlineRoomSide.WHITE : OnlineRoomSide.BLACK;
	}

	private Move copyMove(Move move) {
		return new Move(
			move.from(),
			move.to(),
			move.promotion(),
			move.enPassant(),
			move.castling(),
			move.doublePush()
		);
	}

	private String generatePlayerId() {
		StringBuilder builder = new StringBuilder("player_");
		for (int index = 0; index < PLAYER_ID_LENGTH; index++) {
			int randomIndex = random.nextInt(PLAYER_ID_ALPHABET.length());
			builder.append(PLAYER_ID_ALPHABET.charAt(randomIndex));
		}
		return builder.toString();
	}
}
