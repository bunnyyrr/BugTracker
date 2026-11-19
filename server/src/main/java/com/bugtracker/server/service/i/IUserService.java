package com.bugtracker.server.service.i;

import com.bugtracker.server.entity.User;

public interface IUserService {

    User getProfile(Long userId);
}