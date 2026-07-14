package com.marchub.controller;

import com.marchub.dto.*;
import com.marchub.service.MarchubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    private final MarchubService service;

    public AuthController(MarchubService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest req) {
        return service.register(req);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping("/verify-password")
    public Map<String, Object> verifyPassword(@RequestBody VerifyPasswordRequest req) {
        return service.verifyPassword(req);
    }

    @PostMapping("/check-user")
    public Map<String, Object> checkUser(@RequestBody CheckUserRequest req) {
        return service.checkUser(req);
    }

    @PostMapping("/reset-password")
    public Map<String, Object> resetPassword(@RequestBody ResetPasswordRequest req) {
        return service.resetPassword(req);
    }

    @PostMapping("/update-profile")
    public Map<String, Object> updateProfile(@RequestBody UpdateProfileRequest req) {
        return service.updateProfile(req);
    }
}
