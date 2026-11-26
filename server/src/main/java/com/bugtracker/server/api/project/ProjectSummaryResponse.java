package com.bugtracker.server.api.project;

import com.bugtracker.server.dto.ProjectSummary;
import com.bugtracker.server.entity.Project;
import com.bugtracker.server.entity.enums.Role;

public record ProjectSummaryResponse(Long id, String name, String description, Role role, int openBugCount) {

    public static ProjectSummaryResponse from(ProjectSummary summary) {
        Project project = summary.project();
        return new ProjectSummaryResponse(
                project.getId(), project.getName(), project.getDescription(), summary.role(), summary.openBugCount());
    }
}
