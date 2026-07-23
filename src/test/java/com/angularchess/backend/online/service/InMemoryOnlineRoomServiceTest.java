package com.angularchess.backend.online.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.angularchess.backend.online.dto.CreateOnlineRoomRequest;
import com.angularchess.backend.online.dto.CreateOnlineRoomResponse;
import com.angularchess.backend.online.dto.JoinOnlineRoomResponse;
import com.angularchess.backend.online.dto.RequestOnlineRematchRequest;
import com.angularchess.backend.online.dto.RequestOnlineRematchResponse;
import com.angularchess.backend.online.dto.SubmitOnlineMoveRequest;
import com.angularchess.backend.online.dto.SubmitOnlineMoveResponse;
import com.angularchess.backend.online.model.HostSidePreference;
import com.angularchess.backend.online.model.Move;
import com.angularchess.backend.online.model.OnlineGameSettings;
import com.angularchess.backend.online.model.OnlineRoom;
import com.angularchess.backend.online.model.OnlineRoomSide;
import com.angularchess.backend.online.model.OnlineRoomStatus;
import com.angularchess.backend.online.model.RequestOnlineRematchError;
import com.angularchess.backend.online.model.SideTimeControl;
import com.angularchess.backend.online.model.SubmitOnlineMoveError;
import com.angularchess.backend.online.model.TimeControl;
import com.angularchess.backend.online.repository.InMemoryOnlineRoomRepository;
import com.angularchess.backend.online.websocket.OnlineRoomTopicPublisher;

class InMemoryOnlineRoomServiceTest {

	private static final long NOW = Instant.parse("2026-06-16T20:00:00Z").toEpochMilli();

	private InMemoryOnlineRoomService service;
	private RecordingRoomTopicPublisher roomTopicPublisher;
	private MutableClock clock;

	@BeforeEach
	void setUp() {
		clock = new MutableClock(NOW);
		roomTopicPublisher = new RecordingRoomTopicPublisher();
		service = new InMemoryOnlineRoomService(
			new InMemoryOnlineRoomRepository(),
			new OnlineRoomCodeService(),
			roomTopicPublisher,
			clock
		);
	}

	@Test
	void createRoomCreatesWaitingRoomAndHostSession() {
		CreateOnlineRoomResponse response = service.createRoom(createRoomRequest(HostSidePreference.WHITE));

		assertEquals(OnlineRoomStatus.WAITING, response.room().status());
		assertNotNull(response.room().whitePlayer());
		assertNull(response.room().blackPlayer());
		assertTrue(response.room().moves().isEmpty());
		assertEquals(NOW, response.room().createdAt());
		assertEquals(300_000L, response.room().whiteTimeMs());
		assertEquals(300_000L, response.room().blackTimeMs());
		assertFalse(response.room().whiteRequestedRematch());
		assertFalse(response.room().blackRequestedRematch());
		assertEquals(response.room().code(), response.session().roomCode());
		assertEquals(response.room().whitePlayer().id(), response.session().playerId());
		assertEquals(OnlineRoomSide.WHITE, response.session().playerSide());
		assertEquals(1, roomTopicPublisher.publishCount);
		assertEquals(response.room(), roomTopicPublisher.lastPublishedRoom);
	}

	@Test
	void joinRoomMarksRoomReadyAndAssignsTheFreeSide() {
		CreateOnlineRoomResponse createdRoom = service.createRoom(createRoomRequest(HostSidePreference.BLACK));

		JoinOnlineRoomResponse response = service.joinRoom(createdRoom.room().code());

		assertTrue(response.ok());
		assertEquals(OnlineRoomStatus.READY, response.room().status());
		assertNotNull(response.room().whitePlayer());
		assertNotNull(response.room().blackPlayer());
		assertFalse(response.room().whiteRequestedRematch());
		assertFalse(response.room().blackRequestedRematch());
		assertEquals(OnlineRoomSide.WHITE, response.session().playerSide());
		assertEquals(2, roomTopicPublisher.publishCount);
		assertEquals(response.room(), roomTopicPublisher.lastPublishedRoom);
	}

	@Test
	void submitMoveRejectsANonParticipant() {
		CreateOnlineRoomResponse createdRoom = service.createRoom(createRoomRequest(HostSidePreference.WHITE));
		service.joinRoom(createdRoom.room().code());

		SubmitOnlineMoveResponse response = service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest("player_unknown", new Move(12, 28, null, null, null, null))
		);

		assertFalse(response.ok());
		assertEquals(SubmitOnlineMoveError.NOT_PARTICIPANT, response.error());
	}

	@Test
	void submitMoveRejectsThePlayerWhenItIsNotTheirTurn() {
		CreateOnlineRoomResponse createdRoom = service.createRoom(createRoomRequest(HostSidePreference.BLACK));
		service.joinRoom(createdRoom.room().code());

		SubmitOnlineMoveResponse response = service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(
				createdRoom.session().playerId(),
				new Move(12, 28, null, null, null, null)
			)
		);

		assertFalse(response.ok());
		assertEquals(SubmitOnlineMoveError.NOT_YOUR_TURN, response.error());
	}

	@Test
	void submitMoveRejectsAnIllegalMove() {
		CreateOnlineRoomResponse createdRoom = service.createRoom(createRoomRequest(HostSidePreference.WHITE));
		service.joinRoom(createdRoom.room().code());

		SubmitOnlineMoveResponse response = service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(
				createdRoom.session().playerId(),
				new Move(12, 36, null, null, null, null)
			)
		);

		assertFalse(response.ok());
		assertEquals(SubmitOnlineMoveError.ILLEGAL_MOVE, response.error());
	}

	@Test
	void submitMoveAcceptsTheFirstMoveAndMarksTheRoomAsPlaying() {
		CreateOnlineRoomResponse createdRoom = service.createRoom(createRoomRequest(HostSidePreference.WHITE));
		service.joinRoom(createdRoom.room().code());

		SubmitOnlineMoveResponse response = service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(
				createdRoom.session().playerId(),
				new Move(12, 28, null, null, null, null)
			)
		);

		assertTrue(response.ok());
		assertEquals(OnlineRoomStatus.PLAYING, response.room().status());
		assertEquals(NOW, response.room().startedAt());
		assertEquals(1, response.room().moves().size());
		assertEquals(OnlineRoomSide.WHITE, response.room().moves().getFirst().playedBy());
		assertEquals(300_000L, response.room().blackTimeMs());
		assertEquals(300_000L, response.room().whiteTimeMs());
		assertEquals(OnlineRoomSide.BLACK, response.room().activeClockColor());
		assertEquals(3, roomTopicPublisher.publishCount);
		assertEquals(response.room(), roomTopicPublisher.lastPublishedRoom);
	}

	@Test
	void submitMoveFailureDoesNotPublishAnyNewSnapshot() {
		CreateOnlineRoomResponse createdRoom = service.createRoom(createRoomRequest(HostSidePreference.WHITE));
		service.joinRoom(createdRoom.room().code());

		int publishCountBeforeFailure = roomTopicPublisher.publishCount;
		SubmitOnlineMoveResponse response = service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(
				createdRoom.session().playerId(),
				new Move(12, 36, null, null, null, null)
			)
		);

		assertFalse(response.ok());
		assertEquals(SubmitOnlineMoveError.ILLEGAL_MOVE, response.error());
		assertEquals(publishCountBeforeFailure, roomTopicPublisher.publishCount);
	}

	@Test
	void submitMoveMarksTheRoomFinishedOnCheckmate() {
		CreateOnlineRoomResponse createdRoom = service.createRoom(createRoomRequest(HostSidePreference.WHITE));
		JoinOnlineRoomResponse joinedRoom = service.joinRoom(createdRoom.room().code());

		assertTrue(service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(createdRoom.session().playerId(), new Move(13, 21, null, null, null, null))
		).ok());
		assertTrue(service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(joinedRoom.session().playerId(), new Move(52, 36, null, null, null, true))
		).ok());
		assertTrue(service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(createdRoom.session().playerId(), new Move(14, 30, null, null, null, true))
		).ok());

		SubmitOnlineMoveResponse mate = service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(joinedRoom.session().playerId(), new Move(59, 31, null, null, null, null))
		);

		assertTrue(mate.ok());
		assertEquals(OnlineRoomStatus.FINISHED, mate.room().status());
		assertEquals(NOW, mate.room().finishedAt());
		assertEquals(4, mate.room().moves().size());
	}

	@Test
	void getRoomMaterializesRunningClockUsingElapsedTime() {
		CreateOnlineRoomResponse createdRoom = service.createRoom(createRoomRequest(HostSidePreference.WHITE));
		service.joinRoom(createdRoom.room().code());
		service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(
				createdRoom.session().playerId(),
				new Move(12, 28, null, null, null, null)
			)
		);

		clock.advanceMillis(5_000L);
		var room = service.getRoom(createdRoom.room().code()).room();

		assertNotNull(room);
		assertEquals(300_000L, room.whiteTimeMs());
		assertEquals(295_000L, room.blackTimeMs());
		assertEquals(OnlineRoomSide.BLACK, room.activeClockColor());
	}

	@Test
	void submitMoveRejectsWhenActivePlayerHasAlreadyFlagged() {
		CreateOnlineRoomResponse createdRoom = service.createRoom(createRoomRequest(HostSidePreference.WHITE));
		JoinOnlineRoomResponse joinedRoom = service.joinRoom(createdRoom.room().code());
		assertTrue(service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(createdRoom.session().playerId(), new Move(12, 28, null, null, null, null))
		).ok());

		clock.advanceMillis(301_000L);
		SubmitOnlineMoveResponse response = service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(joinedRoom.session().playerId(), new Move(52, 36, null, null, null, true))
		);

		assertFalse(response.ok());
		assertEquals(SubmitOnlineMoveError.FINISHED, response.error());
		assertEquals(4, roomTopicPublisher.publishCount);
		assertEquals(OnlineRoomStatus.FINISHED, roomTopicPublisher.lastPublishedRoom.status());
		assertEquals(OnlineRoomSide.WHITE, roomTopicPublisher.lastPublishedRoom.timeoutWinner());
		assertEquals(0L, roomTopicPublisher.lastPublishedRoom.blackTimeMs());
	}

	@Test
	void requestRematchRejectsWhenRoomIsNotFinished() {
		CreateOnlineRoomResponse createdRoom = service.createRoom(createRoomRequest(HostSidePreference.WHITE));
		service.joinRoom(createdRoom.room().code());

		RequestOnlineRematchResponse response = service.requestRematch(
			createdRoom.room().code(),
			new RequestOnlineRematchRequest(createdRoom.session().playerId())
		);

		assertFalse(response.ok());
		assertEquals(RequestOnlineRematchError.NOT_FINISHED, response.error());
	}

	@Test
	void requestRematchMarksThePlayerRequestAndPublishesRoomUpdate() {
		CreateOnlineRoomResponse createdRoom = service.createRoom(createRoomRequest(HostSidePreference.WHITE));
		JoinOnlineRoomResponse joinedRoom = service.joinRoom(createdRoom.room().code());
		finishRoomWithFoolsMate(createdRoom, joinedRoom);

		RequestOnlineRematchResponse response = service.requestRematch(
			createdRoom.room().code(),
			new RequestOnlineRematchRequest(createdRoom.session().playerId())
		);

		assertTrue(response.ok());
		assertTrue(response.room().whiteRequestedRematch());
		assertFalse(response.room().blackRequestedRematch());
		assertEquals(7, roomTopicPublisher.publishCount);
		assertEquals(response.room(), roomTopicPublisher.lastPublishedRoom);
	}

	@Test
	void requestRematchResetsTheRoomAndSwapsSidesWhenBothPlayersAccept() {
		CreateOnlineRoomResponse createdRoom = service.createRoom(createRoomRequest(HostSidePreference.WHITE));
		JoinOnlineRoomResponse joinedRoom = service.joinRoom(createdRoom.room().code());
		finishRoomWithFoolsMate(createdRoom, joinedRoom);

		assertTrue(service.requestRematch(
			createdRoom.room().code(),
			new RequestOnlineRematchRequest(createdRoom.session().playerId())
		).ok());

		RequestOnlineRematchResponse response = service.requestRematch(
			createdRoom.room().code(),
			new RequestOnlineRematchRequest(joinedRoom.session().playerId())
		);

		assertTrue(response.ok());
		assertEquals(OnlineRoomStatus.READY, response.room().status());
		assertEquals(joinedRoom.session().playerId(), response.room().whitePlayer().id());
		assertEquals(createdRoom.session().playerId(), response.room().blackPlayer().id());
		assertEquals(OnlineRoomSide.WHITE, response.room().whitePlayer().side());
		assertEquals(OnlineRoomSide.BLACK, response.room().blackPlayer().side());
		assertTrue(response.room().moves().isEmpty());
		assertFalse(response.room().whiteRequestedRematch());
		assertFalse(response.room().blackRequestedRematch());
		assertEquals(300_000L, response.room().whiteTimeMs());
		assertEquals(300_000L, response.room().blackTimeMs());
		assertNull(response.room().startedAt());
		assertNull(response.room().finishedAt());
	}

	@Test
	void rematchFlowPublishesPendingAndResetSnapshotsAndStartsANewGameWithSwappedSides() {
		CreateOnlineRoomResponse createdRoom = service.createRoom(createRoomRequest(HostSidePreference.WHITE));
		JoinOnlineRoomResponse joinedRoom = service.joinRoom(createdRoom.room().code());
		finishRoomWithFoolsMate(createdRoom, joinedRoom);

		RequestOnlineRematchResponse firstRequest = service.requestRematch(
			createdRoom.room().code(),
			new RequestOnlineRematchRequest(createdRoom.session().playerId())
		);

		assertTrue(firstRequest.ok());
		assertEquals(7, roomTopicPublisher.publishCount);
		OnlineRoom pendingRematchRoom = roomTopicPublisher.publishedRooms.get(6);
		assertEquals(OnlineRoomStatus.FINISHED, pendingRematchRoom.status());
		assertTrue(pendingRematchRoom.whiteRequestedRematch());
		assertFalse(pendingRematchRoom.blackRequestedRematch());
		assertEquals(4, pendingRematchRoom.moves().size());
		assertNotNull(pendingRematchRoom.finishedAt());

		RequestOnlineRematchResponse secondRequest = service.requestRematch(
			createdRoom.room().code(),
			new RequestOnlineRematchRequest(joinedRoom.session().playerId())
		);

		assertTrue(secondRequest.ok());
		assertEquals(8, roomTopicPublisher.publishCount);
		OnlineRoom resetRoom = roomTopicPublisher.publishedRooms.get(7);
		assertEquals(secondRequest.room(), resetRoom);
		assertEquals(OnlineRoomStatus.READY, resetRoom.status());
		assertEquals(joinedRoom.session().playerId(), resetRoom.whitePlayer().id());
		assertEquals(createdRoom.session().playerId(), resetRoom.blackPlayer().id());
		assertEquals(OnlineRoomSide.WHITE, resetRoom.whitePlayer().side());
		assertEquals(OnlineRoomSide.BLACK, resetRoom.blackPlayer().side());
		assertNull(resetRoom.timeoutWinner());
		assertNull(resetRoom.activeClockColor());
		assertNull(resetRoom.clockUpdatedAt());
		assertFalse(resetRoom.whiteRequestedRematch());
		assertFalse(resetRoom.blackRequestedRematch());
		assertTrue(resetRoom.moves().isEmpty());
		assertNull(resetRoom.startedAt());
		assertNull(resetRoom.finishedAt());
		assertEquals(300_000L, resetRoom.whiteTimeMs());
		assertEquals(300_000L, resetRoom.blackTimeMs());

		SubmitOnlineMoveResponse restartedGameMove = service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(joinedRoom.session().playerId(), new Move(12, 28, null, null, null, null))
		);

		assertTrue(restartedGameMove.ok());
		assertEquals(OnlineRoomStatus.PLAYING, restartedGameMove.room().status());
		assertEquals(1, restartedGameMove.room().moves().size());
		assertEquals(OnlineRoomSide.WHITE, restartedGameMove.room().moves().getFirst().playedBy());
		assertEquals(joinedRoom.session().playerId(), restartedGameMove.room().whitePlayer().id());
		assertEquals(createdRoom.session().playerId(), restartedGameMove.room().blackPlayer().id());
		assertEquals(NOW, restartedGameMove.room().startedAt());
		assertNull(restartedGameMove.room().finishedAt());
		assertEquals(9, roomTopicPublisher.publishCount);
		assertEquals(restartedGameMove.room(), roomTopicPublisher.lastPublishedRoom);
	}

	private CreateOnlineRoomRequest createRoomRequest(HostSidePreference hostSidePreference) {
		return new CreateOnlineRoomRequest(
			new OnlineGameSettings(
				new TimeControl(
					new SideTimeControl(5, 0),
					new SideTimeControl(5, 0)
				),
				hostSidePreference
			)
		);
	}

	private void finishRoomWithFoolsMate(CreateOnlineRoomResponse createdRoom, JoinOnlineRoomResponse joinedRoom) {
		assertTrue(service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(createdRoom.session().playerId(), new Move(13, 21, null, null, null, null))
		).ok());
		assertTrue(service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(joinedRoom.session().playerId(), new Move(52, 36, null, null, null, true))
		).ok());
		assertTrue(service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(createdRoom.session().playerId(), new Move(14, 30, null, null, null, true))
		).ok());
		assertTrue(service.submitMove(
			createdRoom.room().code(),
			new SubmitOnlineMoveRequest(joinedRoom.session().playerId(), new Move(59, 31, null, null, null, null))
		).ok());
	}

	private static final class RecordingRoomTopicPublisher implements OnlineRoomTopicPublisher {

		private int publishCount;
		private OnlineRoom lastPublishedRoom;
		private final List<OnlineRoom> publishedRooms = new ArrayList<>();

		@Override
		public void publishRoomUpdate(OnlineRoom room) {
			publishCount++;
			lastPublishedRoom = room;
			publishedRooms.add(room);
		}
	}

	private static final class MutableClock extends Clock {

		private long currentTimeMs;

		private MutableClock(long currentTimeMs) {
			this.currentTimeMs = currentTimeMs;
		}

		private void advanceMillis(long millis) {
			currentTimeMs += millis;
		}

		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(java.time.ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return Instant.ofEpochMilli(currentTimeMs);
		}
	}
}
