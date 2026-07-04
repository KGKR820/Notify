package com.kgkr.notify.service;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class WebSocketEventListener {

    private final SimpUserRegistry userRegistry;
    private final NotificationService notificationService;

    public WebSocketEventListener(SimpUserRegistry userRegistry, NotificationService notificationService) {
        this.userRegistry = userRegistry;
        this.notificationService = notificationService;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        if (event.getUser() != null) {
            System.out.println("User connected: " + event.getUser().getName());
        } else {
            System.out.println("User connected (unknown principal)");
        }
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        System.out.println("User disconnected: " + event.getSessionId());
    }

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        if (destination != null && (destination.equals("/user/queue/notifications") || destination.startsWith("/user/"))) {
            if (event.getUser() != null) {
                String userIdStr = event.getUser().getName();
                try {
                    Long userId = Long.parseLong(userIdStr);
                    System.out.println("User " + userId + " subscribed to " + destination + ". Delivering offline notifications...");
                    notificationService.deliverOfflineNotifications(userId);
                } catch (NumberFormatException e) {
                    System.err.println("Failed to parse userId as Long: " + userIdStr);
                }
            }
        }
    }
}
