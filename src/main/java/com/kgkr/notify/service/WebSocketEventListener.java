package com.kgkr.notify.service;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final SimpUserRegistry userRegistry;

    public WebSocketEventListener(SimpUserRegistry userRegistry) {
        this.userRegistry = userRegistry;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        System.out.println("User connected: " + event.getUser().getName());
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        System.out.println("User disconnected: " + event.getSessionId());
    }
}
