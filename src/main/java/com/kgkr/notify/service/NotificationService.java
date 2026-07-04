package com.kgkr.notify.service;

import com.kgkr.notify.dto.Notification;
import com.kgkr.notify.dto.NotificationDto;
import com.kgkr.notify.repo.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepo;

    public NotificationService(SimpMessagingTemplate messagingTemplate,
                               NotificationRepository notificationRepo) {
        this.messagingTemplate = messagingTemplate;
        this.notificationRepo = notificationRepo;
    }

    private static long count = 1;

    public void sendNotification(Long userId, NotificationDto dto) {
        // Save in DB
//        var notif = new Notification();
//        notif.setId(count++);
//        notif.setUserId(userId);
//        notif.setMessage(dto.getMessage());
//        notif.setDelivered(false);
//        notificationRepo.save(notif);

        System.out.println("Notification send to :- " + userId);

        // Send via WebSocket
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                dto
        );

//        notif.setDelivered(true);
//        notificationRepo.save(notif);
    }

    public void broadcastToSubscribers(List<Long> userIds, NotificationDto dto) {
        for (Long userId : userIds) {
            sendNotification(userId, dto);
        }
    }
}
