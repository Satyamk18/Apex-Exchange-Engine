package com.apex.exchange.engine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple broker for /topic (public market feeds) and /queue (private updates)
        registry.enableSimpleBroker("/topic", "/queue");
        // Application prefix for incoming STOMP messages sent from clients
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register standard STOMP endpoint & SockJS fallback
        registry.addEndpoint("/ws-exchange")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/ws-exchange")
                .setAllowedOriginPatterns("*");
    }
}
