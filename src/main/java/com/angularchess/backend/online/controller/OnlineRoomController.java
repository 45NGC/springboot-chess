package com.angularchess.backend.online.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.angularchess.backend.online.dto.CreateOnlineRoomRequest;
import com.angularchess.backend.online.dto.CreateOnlineRoomResponse;
import com.angularchess.backend.online.dto.GetOnlineRoomResponse;
import com.angularchess.backend.online.dto.JoinOnlineRoomRequest;
import com.angularchess.backend.online.dto.JoinOnlineRoomResponse;
import com.angularchess.backend.online.dto.SubmitOnlineMoveRequest;
import com.angularchess.backend.online.dto.SubmitOnlineMoveResponse;
import com.angularchess.backend.online.service.OnlineRoomService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/online/rooms")
public class OnlineRoomController {

	private final OnlineRoomService onlineRoomService;

	public OnlineRoomController(OnlineRoomService onlineRoomService) {
		this.onlineRoomService = onlineRoomService;
	}

	@PostMapping
	public CreateOnlineRoomResponse createRoom(@Valid @RequestBody CreateOnlineRoomRequest request) {
		return onlineRoomService.createRoom(request);
	}

	@PostMapping("/{code}/join")
	public JoinOnlineRoomResponse joinRoom(
		@PathVariable String code,
		@Valid @RequestBody JoinOnlineRoomRequest request
	) {
		return onlineRoomService.joinRoom(code);
	}

	@GetMapping("/{code}")
	public GetOnlineRoomResponse getRoom(@PathVariable String code) {
		return onlineRoomService.getRoom(code);
	}

	@PostMapping("/{code}/moves")
	public SubmitOnlineMoveResponse submitMove(
		@PathVariable String code,
		@Valid @RequestBody SubmitOnlineMoveRequest request
	) {
		return onlineRoomService.submitMove(code, request);
	}
}
