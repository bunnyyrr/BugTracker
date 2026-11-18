package com.bugtracker.server.service.i;

import com.bugtracker.server.dto.ProjectResponse;
import com.bugtracker.server.dto.ProjectSummary;

import java.util.List;

public interface IProjectService {

    ProjectResponse createProject(Long creatorId, String name, String description);

    List<ProjectSummary> getProjects(Long userId);

    ProjectResponse getProject(Long actorId, Long projectId);
}
