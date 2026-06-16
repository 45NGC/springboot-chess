package com.angularchess.backend.online.service;

import java.security.SecureRandom;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class OnlineRoomCodeService {

	private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final int DEFAULT_CODE_LENGTH = 6;

	private final SecureRandom random = new SecureRandom();

	public String generateCode() {
		StringBuilder builder = new StringBuilder(DEFAULT_CODE_LENGTH);
		for (int index = 0; index < DEFAULT_CODE_LENGTH; index++) {
			int randomIndex = random.nextInt(ALPHABET.length());
			builder.append(ALPHABET.charAt(randomIndex));
		}
		return builder.toString();
	}

	public String normalizeCode(String value) {
		if (value == null) {
			return "";
		}

		String normalized = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
		return normalized.length() <= DEFAULT_CODE_LENGTH
			? normalized
			: normalized.substring(0, DEFAULT_CODE_LENGTH);
	}
}
