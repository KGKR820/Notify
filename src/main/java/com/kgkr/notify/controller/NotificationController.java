package com.kgkr.notify.controller;

import com.kgkr.notify.dto.ChannelEvent;
import com.kgkr.notify.dto.Notification;
import com.kgkr.notify.dto.NotificationDto;
import com.kgkr.notify.service.AuthService;
import com.kgkr.notify.service.NotificationService;
import com.kgkr.notify.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final SubscriptionService subscriptionService;
    private final AuthService authService;

    public NotificationController(NotificationService notificationService,
                                  SubscriptionService subscriptionService,
                                  AuthService authService) {
        this.notificationService = notificationService;
        this.subscriptionService = subscriptionService;
        this.authService = authService;
    }

    @PostMapping("/publish")
    public String publish(@RequestBody ChannelEvent event) {
        var subscribers = subscriptionService.getSubscribers(event.getChannelId());

        NotificationDto dto = new NotificationDto(
                "New video: " + event.getVideoTitle(),
                event.getChannelId()
        );

        notificationService.broadcastToSubscribers(subscribers, dto);
        return "Notification sent to " + subscribers.size() + " subscribers.";
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getHistory(
            @RequestHeader("X-Auth-Token") String token,
            @RequestParam Long userId) {
        if (!authService.validateToken(userId, token)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(notificationService.getNotificationHistory(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable String id) {
        Notification notif = notificationService.getNotification(id);
        if (notif == null) {
            return ResponseEntity.notFound().build();
        }
        if (!authService.validateToken(notif.getUserId(), token)) {
            return ResponseEntity.status(401).build();
        }
        Notification updated = notificationService.markAsRead(id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<String> markAllAsRead(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long userId) {
        if (!authService.validateToken(userId, token)) {
            return ResponseEntity.status(401).body("Unauthorized: Invalid session token.");
        }
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read for user " + userId);
    }
}