package com.angularchess.backend.online.service;

import com.angularchess.backend.online.dto.CreateOnlineRoomRequest;
import com.angularchess.backend.online.dto.CreateOnlineRoomResponse;
import com.angularchess.backend.online.dto.GetOnlineRoomResponse;
import com.angularchess.backend.online.dto.JoinOnlineRoomResponse;
import com.angularchess.backend.online.dto.SubmitOnlineMoveRequest;
import com.angularchess.backend.online.dto.SubmitOnlineMoveResponse;

public interface OnlineRoomService {

	CreateOnlineRoomResponse createRoom(CreateOnlineRoomRequest request);

	JoinOnlineRoomResponse joinRoom(String rawCode);

	GetOnlineRoomResponse getRoom(String rawCode);

	SubmitOnlineMoveResponse submitMove(String rawCode, SubmitOnlineMoveRequest request);
}
