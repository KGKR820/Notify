package com.kgkr.notify.controller;

import com.kgkr.notify.dto.SubscriptionRequest;
import com.kgkr.notify.service.AuthService;
import com.kgkr.notify.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final AuthService authService;

    public SubscriptionController(SubscriptionService subscriptionService, AuthService authService) {
        this.subscriptionService = subscriptionService;
        this.authService = authService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(
            @RequestHeader("X-Auth-Token") String token,
            @RequestBody SubscriptionRequest request) {
        if (!authService.validateToken(request.getUserId(), token)) {
            return ResponseEntity.status(401).body("Unauthorized: Invalid session token.");
        }
        subscriptionService.subscribe(request);
        return ResponseEntity.ok("User " + request.getUserId() + " subscribed to channel " + request.getChannelId());
    }
}
