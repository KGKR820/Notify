package com.kgkr.notify.controller;

import com.kgkr.notify.dto.ChannelEvent;
import com.kgkr.notify.dto.NotificationDto;
import com.kgkr.notify.service.NotificationService;
import com.kgkr.notify.service.SubscriptionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}