package com.bugtracker.server.service.i;

import com.bugtracker.server.entity.User;

public interface IUserService {
    User getProfile(Long userId);

    User updateProfile(Long userId, String username, String email, String newPassword);

    void deleteProfile(Long userId);
}
