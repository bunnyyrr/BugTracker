package com.bugtracker.server.dto;

import com.bugtracker.server.entity.enums.Role;

public record ProjectResponse(Long id, String name, String description, Role myRole) {
}
