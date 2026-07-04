package com.kgkr.notify.controller;

import com.kgkr.notify.dto.ChannelEvent;
import com.kgkr.notify.dto.Notification;
import com.kgkr.notify.dto.NotificationDto;
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

    public NotificationController(NotificationService notificationService,
                                  SubscriptionService subscriptionService) {
        this.notificationService = notificationService;
        this.subscriptionService = subscriptionService;
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
    public List<Notification> getHistory(@RequestParam Long userId) {
        return notificationService.getNotificationHistory(userId);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable String id) {
        Notification updated = notificationService.markAsRead(id);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<String> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read for user " + userId);
    }
}