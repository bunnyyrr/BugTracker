package com.bugtracker.server.service.i;

import com.bugtracker.server.entity.User;

public interface IAuthService {
    User register(String username, String email, String password);

    String login(String login, String password);

    User authenticate(String token);
}
