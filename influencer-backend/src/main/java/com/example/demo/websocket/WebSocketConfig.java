package com.example.demo.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * Configures STOMP WebSocket with SockJS fallback.
 * Messages sent to /app/... are routed to @MessageMapping handlers.
 * The simple in-memory broker handles subscriptions to /topic/... and /queue/...
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Destinations prefixed with /topic or /queue are handled by the in-memory broker
        registry.enableSimpleBroker("/topic", "/queue");
        // Destinations prefixed with /app are routed to @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS endpoint — frontend connects here
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
