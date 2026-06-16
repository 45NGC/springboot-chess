package com.angularchess.backend.online.repository;

import java.util.Optional;

import com.angularchess.backend.online.model.OnlineRoom;

public interface OnlineRoomRepository {

	boolean existsByCode(String code);

	Optional<OnlineRoom> findByCode(String code);

	void save(OnlineRoom room);
}
