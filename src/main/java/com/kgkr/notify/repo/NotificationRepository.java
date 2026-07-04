package com.kgkr.notify.repo;

import com.kgkr.notify.dto.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdAndDelivered(Long userId, boolean delivered);
}
