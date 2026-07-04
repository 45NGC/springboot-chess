package com.angularchess.backend.online.websocket;

public final class OnlineRoomTopics {

	private OnlineRoomTopics() {
	}

	public static String roomUpdates(String roomCode) {
		return "/topic/online/rooms/" + roomCode;
	}
}
