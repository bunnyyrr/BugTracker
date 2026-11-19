package com.bugtracker.server.dto;

import com.bugtracker.server.entity.enums.Role;

public record ProjectSummary(Long id, String name, String description, Role role, long openBugCount) {
}
