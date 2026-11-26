package com.bugtracker.server.dto;

import com.bugtracker.server.entity.Project;
import com.bugtracker.server.entity.enums.Role;

public record ProjectSummary(Project project, Role role, int openBugCount) {
}
