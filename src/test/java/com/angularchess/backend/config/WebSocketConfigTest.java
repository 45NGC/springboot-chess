package com.angularchess.backend.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

@SpringBootTest
class WebSocketConfigTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void websocketInfrastructureIsRegistered() {
		var handlerMapping = applicationContext.getBean(
			"stompWebSocketHandlerMapping",
			SimpleUrlHandlerMapping.class
		);
		var brokerHandler = applicationContext.getBean(SimpleBrokerMessageHandler.class);

		assertNotNull(handlerMapping);
		assertNotNull(brokerHandler);
		assertTrue(handlerMapping.getHandlerMap().containsKey(WebSocketDestinations.STOMP_ENDPOINT));
	}
}
