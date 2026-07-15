package com.angularchess.backend.online.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;

import com.angularchess.backend.online.dto.OnlineRoomUpdateEvent;
import com.angularchess.backend.online.model.OnlinePlayerPresence;
import com.angularchess.backend.online.model.OnlineRoom;
import com.angularchess.backend.online.model.OnlineRoomPlayer;
import com.angularchess.backend.online.model.OnlineRoomSide;
import com.angularchess.backend.online.model.OnlineRoomStatus;
import com.angularchess.backend.online.model.SideTimeControl;
import com.angularchess.backend.online.model.TimeControl;

class StompOnlineRoomTopicPublisherTest {

	@Test
	void publishRoomUpdateSendsSnapshotToTheRoomTopic() {
		RecordingMessageChannel messageChannel = new RecordingMessageChannel();
		SimpMessagingTemplate messagingTemplate = new SimpMessagingTemplate(messageChannel);
		StompOnlineRoomTopicPublisher publisher = new StompOnlineRoomTopicPublisher(messagingTemplate);
		OnlineRoom room = new OnlineRoom(
			"ABC123",
			OnlineRoomStatus.READY,
			new OnlineRoomPlayer("player_white", OnlineRoomSide.WHITE, OnlinePlayerPresence.CONNECTED, 1L),
			new OnlineRoomPlayer("player_black", OnlineRoomSide.BLACK, OnlinePlayerPresence.CONNECTED, 2L),
			new TimeControl(new SideTimeControl(5, 0), new SideTimeControl(5, 0)),
			300_000L,
			300_000L,
			null,
			null,
			null,
			false,
			false,
			List.of(),
			1L,
			null,
			null
		);

		publisher.publishRoomUpdate(room);

		assertNotNull(messageChannel.lastMessage);

		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(messageChannel.lastMessage);
		assertEquals(SimpMessageType.MESSAGE, headers.getMessageType());
		assertEquals(OnlineRoomTopics.roomUpdates("ABC123"), headers.getDestination());

		Object payload = messageChannel.lastMessage.getPayload();
		assertInstanceOf(OnlineRoomUpdateEvent.class, payload);
		assertEquals(new OnlineRoomUpdateEvent(room), payload);
	}

	private static final class RecordingMessageChannel implements MessageChannel {

		private Message<?> lastMessage;

		@Override
		public boolean send(Message<?> message) {
			lastMessage = message;
			return true;
		}

		@Override
		public boolean send(Message<?> message, long timeout) {
			lastMessage = message;
			return true;
		}
	}
}
