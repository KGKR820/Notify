package com.kgkr.notify.service;

import com.kgkr.notify.dto.User;
import com.kgkr.notify.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepo;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User loginOrCreate(Long userId, String name) {
        return userRepo.findById(userId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(userId);
                    newUser.setName(name != null && !name.trim().isEmpty() ? name : "User_" + userId);
                    newUser.setToken(UUID.randomUUID().toString());
                    return userRepo.save(newUser);
                });
    }

    public boolean validateToken(Long userId, String token) {
        if (userId == null || token == null) {
            return false;
        }
        return userRepo.findById(userId)
                .map(user -> token.equals(user.getToken()))
                .orElse(false);
    }
}
