package com.angularchess.backend.online.repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Repository;

import com.angularchess.backend.online.model.OnlineRoom;

@Repository
public class InMemoryOnlineRoomRepository implements OnlineRoomRepository {

	private final ConcurrentMap<String, OnlineRoom> rooms = new ConcurrentHashMap<>();

	@Override
	public boolean existsByCode(String code) {
		return rooms.containsKey(code);
	}

	@Override
	public Optional<OnlineRoom> findByCode(String code) {
		return Optional.ofNullable(rooms.get(code));
	}

	@Override
	public void save(OnlineRoom room) {
		rooms.put(room.code(), room);
	}
}
