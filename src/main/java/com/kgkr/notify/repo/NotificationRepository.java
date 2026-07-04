package com.kgkr.notify.repo;

import com.kgkr.notify.dto.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, Long> {
}
