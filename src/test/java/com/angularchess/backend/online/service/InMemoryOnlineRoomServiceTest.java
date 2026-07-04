package com.angularchess.backend.online.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.angularchess.backend.online.dto.CreateOnlineRoomRequest;
import com.angularchess.backend.online.dto.CreateOnlineRoomResponse;
import com.angularchess.backend.online.dto.JoinOnlineRoomResponse;
import com.angularchess.backend.online.dto.SubmitOnlineMoveRequest;
import com.angularchess.backend.online.dto.SubmitOnlineMoveResponse;
import com.angularchess.backend.online.model.HostSidePreference;
import com.angularchess.backend.online.model.Move;
import com.angularchess.backend.online.model.OnlineGameSettings;
import com.angularchess.backend.online.model.OnlineRoom;
import com.angularchess.backend.online.model.OnlineRoomSide;
import com.angularchess.backend.online.model.OnlineRoomStatus;
import com.angularchess.backend.online.model.SideTimeControl;
import com.angularchess.backend.online.model.SubmitOnlineMoveError;
import com.angularchess.backend.online.model.TimeControl;
import com.angularchess.backend.online.repository.InMemoryOnlineRoomRepository;
import com.angularchess.backend.online.websocket.OnlineRoomTopicPublisher;

class InMemoryOnlineRoomServiceTest {

	private static final long NOW = Instant.parse("2026-06-16T20:00:00Z").toEpochMilli();

	private InMemoryOnlineRoomService service;
	private RecordingRoomTopicPublisher roomTopicPublisher;

	@BeforeEach
	void setUp() {
		Clock fixedClock = Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC);
		roomTopicPublisher = new RecordingRoomTopicPublisher();
		service = new InMemoryOnlineRoomService(
			new InMemoryOnlineRoomRepository(),
			new OnlineRoomCodeService(),
			roomTopicPublisher,
			fixedClock
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

	private static final class RecordingRoomTopicPublisher implements OnlineRoomTopicPublisher {

		private int publishCount;
		private OnlineRoom lastPublishedRoom;

		@Override
		public void publishRoomUpdate(OnlineRoom room) {
			publishCount++;
			lastPublishedRoom = room;
		}
	}
}
