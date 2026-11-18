package com.bugtracker.server.controllers;

import com.bugtracker.server.dto.UserResponse;
import com.bugtracker.server.service.i.IUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/me")
    public UserResponse me(@AuthenticationPrincipal Long actorId) {
        return UserResponse.withEmail(userService.getProfile(actorId));
    }
}
