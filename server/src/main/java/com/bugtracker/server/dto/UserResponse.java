package com.bugtracker.server.dto;

import com.bugtracker.server.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(Long id, String username, String email) {

    public static UserResponse of(User user) {
        return new UserResponse(user.getId(), user.getUsername(), null);
    }
}
