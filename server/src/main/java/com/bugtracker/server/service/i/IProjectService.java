package com.bugtracker.server.service.i;

import com.bugtracker.server.dto.ProjectSummary;
import com.bugtracker.server.entity.Project;

import java.util.List;

public interface IProjectService {
    Project createProject(Long creatorId, String name, String description);

    List<ProjectSummary> getProjects(Long userId);

    Project getProject(Long actorId, Long projectId);

    Project updateProject(Long actorId, Long projectId, String name, String description);

    void deleteProject(Long actorId, Long projectId);
}
