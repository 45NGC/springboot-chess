package com.angularchess.backend.online.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.angularchess.backend.online.dto.CreateOnlineRoomRequest;
import com.angularchess.backend.online.dto.CreateOnlineRoomResponse;
import com.angularchess.backend.online.dto.GetOnlineRoomResponse;
import com.angularchess.backend.online.dto.JoinOnlineRoomResponse;
import com.angularchess.backend.online.dto.RequestOnlineRematchRequest;
import com.angularchess.backend.online.dto.RequestOnlineRematchResponse;
import com.angularchess.backend.online.dto.SubmitOnlineMoveRequest;
import com.angularchess.backend.online.dto.SubmitOnlineMoveResponse;
import com.angularchess.backend.online.model.JoinOnlineRoomError;
import com.angularchess.backend.online.model.Move;
import com.angularchess.backend.online.model.OnlineMoveRecord;
import com.angularchess.backend.online.model.OnlinePlayerPresence;
import com.angularchess.backend.online.model.OnlineRoom;
import com.angularchess.backend.online.model.OnlineRoomPlayer;
import com.angularchess.backend.online.model.OnlineRoomSession;
import com.angularchess.backend.online.model.OnlineRoomSide;
import com.angularchess.backend.online.model.OnlineRoomStatus;
import com.angularchess.backend.online.model.RequestOnlineRematchError;
import com.angularchess.backend.online.model.SideTimeControl;
import com.angularchess.backend.online.model.SubmitOnlineMoveError;
import com.angularchess.backend.online.model.TimeControl;
import com.angularchess.backend.online.service.OnlineRoomService;

class OnlineRoomControllerTest {

	private static final long NOW = Instant.parse("2026-06-28T18:00:00Z").toEpochMilli();

	private MockMvc mockMvc;
	private StubOnlineRoomService onlineRoomService;

	@BeforeEach
	void setUp() {
		onlineRoomService = new StubOnlineRoomService();
		mockMvc = MockMvcBuilders
			.standaloneSetup(new OnlineRoomController(onlineRoomService))
			.build();
	}

	@Test
	void createRoomReturnsCreatedSnapshotAndSession() throws Exception {
		onlineRoomService.createRoomResponse = new CreateOnlineRoomResponse(
			sampleWaitingRoom(),
			new OnlineRoomSession("ABC123", "player_host", OnlineRoomSide.WHITE)
		);

		mockMvc.perform(post("/api/online/rooms")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "settings": {
					    "timeControlSettings": {
					      "white": { "baseMinutes": 5, "incrementSeconds": 0 },
					      "black": { "baseMinutes": 5, "incrementSeconds": 0 }
					    },
					    "hostSidePreference": "white"
					  }
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.room.code").value("ABC123"))
			.andExpect(jsonPath("$.room.status").value("waiting"))
			.andExpect(jsonPath("$.session.playerId").value("player_host"))
			.andExpect(jsonPath("$.session.playerSide").value("white"));

		assertNotNull(onlineRoomService.lastCreateRoomRequest);
	}

	@Test
	void joinRoomReturnsSuccessPayload() throws Exception {
		onlineRoomService.joinRoomResponse = JoinOnlineRoomResponse.success(
			sampleReadyRoom(),
			new OnlineRoomSession("ABC123", "player_guest", OnlineRoomSide.BLACK)
		);

		mockMvc.perform(post("/api/online/rooms/ABC123/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "code": "ABC123"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ok").value(true))
			.andExpect(jsonPath("$.room.status").value("ready"))
			.andExpect(jsonPath("$.session.playerId").value("player_guest"));
	}

	@Test
	void joinRoomReturnsDomainErrorPayload() throws Exception {
		onlineRoomService.joinRoomResponse = JoinOnlineRoomResponse.failure(JoinOnlineRoomError.FULL);

		mockMvc.perform(post("/api/online/rooms/ABC123/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "code": "ABC123"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ok").value(false))
			.andExpect(jsonPath("$.error").value("full"))
			.andExpect(jsonPath("$.room").doesNotExist())
			.andExpect(jsonPath("$.session").doesNotExist());
	}

	@Test
	void getRoomReturnsNullWhenRoomDoesNotExist() throws Exception {
		onlineRoomService.getRoomResponse = new GetOnlineRoomResponse(null);

		mockMvc.perform(get("/api/online/rooms/ZZZZZZ"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.room").isEmpty());
	}

	@Test
	void submitMoveReturnsUpdatedRoomOnSuccess() throws Exception {
		onlineRoomService.submitMoveResponse = SubmitOnlineMoveResponse.success(samplePlayingRoom());

		mockMvc.perform(post("/api/online/rooms/ABC123/moves")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "playerId": "player_host",
					  "move": {
					    "from": 12,
					    "to": 28
					  }
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ok").value(true))
			.andExpect(jsonPath("$.room.status").value("playing"))
			.andExpect(jsonPath("$.room.moves[0].move.from").value(12))
			.andExpect(jsonPath("$.room.moves[0].playedBy").value("white"));
	}

	@Test
	void submitMoveReturnsIllegalMoveError() throws Exception {
		onlineRoomService.submitMoveResponse = SubmitOnlineMoveResponse.failure(SubmitOnlineMoveError.ILLEGAL_MOVE);

		mockMvc.perform(post("/api/online/rooms/ABC123/moves")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "playerId": "player_host",
					  "move": {
					    "from": 12,
					    "to": 36
					  }
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ok").value(false))
			.andExpect(jsonPath("$.error").value("illegalMove"))
			.andExpect(jsonPath("$.room").doesNotExist());
	}

	@Test
	void requestRematchReturnsUpdatedRoomOnSuccess() throws Exception {
		onlineRoomService.requestOnlineRematchResponse = RequestOnlineRematchResponse.success(sampleFinishedRoom(true, false));

		mockMvc.perform(post("/api/online/rooms/ABC123/rematch")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "playerId": "player_host"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ok").value(true))
			.andExpect(jsonPath("$.room.whiteRequestedRematch").value(true))
			.andExpect(jsonPath("$.room.blackRequestedRematch").value(false));
	}

	@Test
	void requestRematchReturnsDomainErrorPayload() throws Exception {
		onlineRoomService.requestOnlineRematchResponse = RequestOnlineRematchResponse.failure(RequestOnlineRematchError.NOT_FINISHED);

		mockMvc.perform(post("/api/online/rooms/ABC123/rematch")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "playerId": "player_host"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ok").value(false))
			.andExpect(jsonPath("$.error").value("notFinished"))
			.andExpect(jsonPath("$.room").doesNotExist());
	}

	private OnlineRoom sampleWaitingRoom() {
		return new OnlineRoom(
			"ABC123",
			OnlineRoomStatus.WAITING,
			new OnlineRoomPlayer("player_host", OnlineRoomSide.WHITE, OnlinePlayerPresence.CONNECTED, NOW),
			null,
			defaultTimeControl(),
			300_000L,
			300_000L,
			null,
			null,
			null,
			false,
			false,
			List.of(),
			NOW,
			null,
			null
		);
	}

	private OnlineRoom sampleReadyRoom() {
		return new OnlineRoom(
			"ABC123",
			OnlineRoomStatus.READY,
			new OnlineRoomPlayer("player_host", OnlineRoomSide.WHITE, OnlinePlayerPresence.CONNECTED, NOW),
			new OnlineRoomPlayer("player_guest", OnlineRoomSide.BLACK, OnlinePlayerPresence.CONNECTED, NOW),
			defaultTimeControl(),
			300_000L,
			300_000L,
			null,
			null,
			null,
			false,
			false,
			List.of(),
			NOW,
			null,
			null
		);
	}

	private OnlineRoom samplePlayingRoom() {
		return new OnlineRoom(
			"ABC123",
			OnlineRoomStatus.PLAYING,
			new OnlineRoomPlayer("player_host", OnlineRoomSide.WHITE, OnlinePlayerPresence.CONNECTED, NOW),
			new OnlineRoomPlayer("player_guest", OnlineRoomSide.BLACK, OnlinePlayerPresence.CONNECTED, NOW),
			defaultTimeControl(),
			302_000L,
			300_000L,
			OnlineRoomSide.BLACK,
			NOW,
			null,
			false,
			false,
			List.of(new OnlineMoveRecord(
				new Move(12, 28, null, null, null, true),
				OnlineRoomSide.WHITE,
				NOW
			)),
			NOW,
			NOW,
			null
		);
	}

	private OnlineRoom sampleFinishedRoom(boolean whiteRequestedRematch, boolean blackRequestedRematch) {
		return new OnlineRoom(
			"ABC123",
			OnlineRoomStatus.FINISHED,
			new OnlineRoomPlayer("player_host", OnlineRoomSide.WHITE, OnlinePlayerPresence.CONNECTED, NOW),
			new OnlineRoomPlayer("player_guest", OnlineRoomSide.BLACK, OnlinePlayerPresence.CONNECTED, NOW),
			defaultTimeControl(),
			300_000L,
			0L,
			null,
			null,
			OnlineRoomSide.WHITE,
			whiteRequestedRematch,
			blackRequestedRematch,
			List.of(new OnlineMoveRecord(
				new Move(59, 31, null, null, null, null),
				OnlineRoomSide.BLACK,
				NOW
			)),
			NOW,
			NOW,
			NOW
		);
	}

	private TimeControl defaultTimeControl() {
		return new TimeControl(
			new SideTimeControl(5, 0),
			new SideTimeControl(5, 0)
		);
	}

	private static final class StubOnlineRoomService implements OnlineRoomService {

		private CreateOnlineRoomRequest lastCreateRoomRequest;
		private CreateOnlineRoomResponse createRoomResponse;
		private JoinOnlineRoomResponse joinRoomResponse;
		private GetOnlineRoomResponse getRoomResponse;
		private SubmitOnlineMoveResponse submitMoveResponse;
		private RequestOnlineRematchResponse requestOnlineRematchResponse;

		@Override
		public CreateOnlineRoomResponse createRoom(CreateOnlineRoomRequest request) {
			this.lastCreateRoomRequest = request;
			return createRoomResponse;
		}

		@Override
		public JoinOnlineRoomResponse joinRoom(String rawCode) {
			return joinRoomResponse;
		}

		@Override
		public GetOnlineRoomResponse getRoom(String rawCode) {
			return getRoomResponse;
		}

		@Override
		public SubmitOnlineMoveResponse submitMove(String rawCode, SubmitOnlineMoveRequest request) {
			return submitMoveResponse;
		}

		@Override
		public RequestOnlineRematchResponse requestRematch(String rawCode, RequestOnlineRematchRequest request) {
			return requestOnlineRematchResponse;
		}
	}
}
