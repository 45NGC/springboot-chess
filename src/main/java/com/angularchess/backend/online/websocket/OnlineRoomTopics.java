package com.angularchess.backend.online.websocket;

import com.angularchess.backend.config.WebSocketDestinations;

public final class OnlineRoomTopics {

	public static final String ONLINE_ROOMS_TOPIC_PREFIX =
		WebSocketDestinations.SIMPLE_BROKER_PREFIX + "/online/rooms";

	private OnlineRoomTopics() {
	}

	public static String roomUpdates(String roomCode) {
		return ONLINE_ROOMS_TOPIC_PREFIX + "/" + roomCode;
	}
}
