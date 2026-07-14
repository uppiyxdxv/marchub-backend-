package com.marchub.controller;

import com.marchub.service.MarchubService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    private final MarchubService service;

    public UserController(MarchubService service) {
        this.service = service;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {
        return service.getUsers();
    }
}
