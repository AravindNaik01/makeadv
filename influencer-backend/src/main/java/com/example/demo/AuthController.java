package com.example.demo;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    // REGISTER API
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return service.register(user);
    }

    // LOGIN API
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return service.login(user);
    }
}