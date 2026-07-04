package com.kgkr.notify.config;

import com.kgkr.notify.service.AuthService;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthService authService;

    public WebSocketConfig(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Frontend connects here: ws://localhost:8080/ws
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for client -> server messages
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for server -> client messages
        registry.enableSimpleBroker("/topic", "/queue");

        // For user-specific messages (/user/{id}/queue/...)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // read userId and token from headers
                    String userId = accessor.getFirstNativeHeader("login");
                    String token = accessor.getFirstNativeHeader("auth-token");
                    if (userId != null && token != null) {
                        try {
                            Long uId = Long.parseLong(userId);
                            if (authService.validateToken(uId, token)) {
                                Principal principal = () -> userId;
                                accessor.setUser(principal);
                            } else {
                                throw new IllegalArgumentException("Invalid authentication token!");
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("User ID must be a numeric value!");
                        }
                    } else {
                        throw new IllegalArgumentException("Missing login or auth-token headers!");
                    }
                }
                return message;
            }
        });
    }
}