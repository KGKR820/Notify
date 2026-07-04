package com.kgkr.notify.repo;

import com.kgkr.notify.dto.Channel;
import com.kgkr.notify.dto.Subscription;
import com.kgkr.notify.dto.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface SubscriptionRepo extends MongoRepository<Subscription, Long> {

    List<Subscription> findByChannelId(Long channelId);
}
