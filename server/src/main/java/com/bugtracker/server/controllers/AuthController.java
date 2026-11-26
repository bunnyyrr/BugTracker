package com.bugtracker.server.controllers;

import com.bugtracker.server.dto.LoginRequest;
import com.bugtracker.server.dto.LoginResponse;
import com.bugtracker.server.dto.RegisterRequest;
import com.bugtracker.server.dto.UserResponse;
import com.bugtracker.server.service.i.IAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody RegisterRequest request) {
        return UserResponse.of(authService.register(request.username(), request.email(), request.password()));
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return new LoginResponse(authService.login(request.login(), request.password()));
    }
}
