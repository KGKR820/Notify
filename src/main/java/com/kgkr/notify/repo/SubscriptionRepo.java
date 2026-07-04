package com.kgkr.notify.repo;

import com.kgkr.notify.dto.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SubscriptionRepo extends MongoRepository<Subscription, String> {

    List<Subscription> findByChannelId(Long channelId);
}
