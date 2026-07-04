package com.angularchess.backend.online.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.angularchess.backend.online.dto.OnlineRoomUpdateEvent;
import com.angularchess.backend.online.model.OnlineRoom;

@Component
public class StompOnlineRoomTopicPublisher implements OnlineRoomTopicPublisher {

	private final SimpMessagingTemplate messagingTemplate;

	public StompOnlineRoomTopicPublisher(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	@Override
	public void publishRoomUpdate(OnlineRoom room) {
		messagingTemplate.convertAndSend(
			OnlineRoomTopics.roomUpdates(room.code()),
			new OnlineRoomUpdateEvent(room)
		);
	}
}
