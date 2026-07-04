package com.kgkr.notify.repo;

import com.kgkr.notify.dto.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, Long> {
}
