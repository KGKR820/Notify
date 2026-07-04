package com.kgkr.notify.controller;

import com.kgkr.notify.dto.User;
import com.kgkr.notify.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public User login(@RequestBody User loginRequest) {
        return authService.loginOrCreate(loginRequest.getId(), loginRequest.getName());
    }
}
