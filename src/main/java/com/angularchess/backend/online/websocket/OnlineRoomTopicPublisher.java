package com.angularchess.backend.online.websocket;

import com.angularchess.backend.online.model.OnlineRoom;

public interface OnlineRoomTopicPublisher {

	void publishRoomUpdate(OnlineRoom room);
}
