package com.kgkr.notify.service;

import com.kgkr.notify.dto.Notification;
import com.kgkr.notify.dto.NotificationDto;
import com.kgkr.notify.repo.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepo;
    private final SimpUserRegistry userRegistry;

    public NotificationService(SimpMessagingTemplate messagingTemplate,
                               NotificationRepository notificationRepo,
                               SimpUserRegistry userRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.notificationRepo = notificationRepo;
        this.userRegistry = userRegistry;
    }

    public boolean isUserOnline(Long userId) {
        return userRegistry.getUser(userId.toString()) != null;
    }

    public void sendNotification(Long userId, NotificationDto dto) {
        boolean online = isUserOnline(userId);

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setChannelId(dto.getChannelId());
        notification.setMessage(dto.getMessage());
        notification.setDelivered(online);
        notification.setRead(false);
        notification.setTimestamp(Instant.now());

        // Save in DB to get auto-generated ID
        notification = notificationRepo.save(notification);

        System.out.println("Notification processed for user " + userId + " (Online: " + online + ")");

        if (online) {
            // Send via WebSocket
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notification
            );
        }
    }

    public void deliverOfflineNotifications(Long userId) {
        List<Notification> undelivered = notificationRepo.findByUserIdAndDelivered(userId, false);
        if (!undelivered.isEmpty()) {
            System.out.println("Delivering " + undelivered.size() + " offline notifications to user " + userId);
            for (Notification notif : undelivered) {
                // Send via WebSocket
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/notifications",
                        notif
                );
                // Mark as delivered
                notif.setDelivered(true);
                notificationRepo.save(notif);
            }
        }
    }

    public void broadcastToSubscribers(List<Long> userIds, NotificationDto dto) {
        for (Long userId : userIds) {
            sendNotification(userId, dto);
        }
    }

    public List<Notification> getNotificationHistory(Long userId) {
        return notificationRepo.findByUserId(userId);
    }

    public Notification getNotification(String id) {
        return notificationRepo.findById(id).orElse(null);
    }

    public Notification markAsRead(String id) {
        return notificationRepo.findById(id)
                .map(notif -> {
                    notif.setRead(true);
                    return notificationRepo.save(notif);
                })
                .orElse(null);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepo.findByUserId(userId);
        for (Notification notif : notifications) {
            if (!notif.isRead()) {
                notif.setRead(true);
                notificationRepo.save(notif);
            }
        }
    }
}
