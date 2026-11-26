package com.bugtracker.server.controllers;

import com.bugtracker.server.api.project.ProjectSummaryResponse;
import com.bugtracker.server.dto.CreateProjectRequest;
import com.bugtracker.server.dto.ProjectResponse;
import com.bugtracker.server.entity.Project;
import com.bugtracker.server.entity.enums.Role;
import com.bugtracker.server.service.i.IProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final IProjectService projectService;

    public ProjectController(IProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectSummaryResponse> list(@AuthenticationPrincipal Long actorId) {
        return projectService.getProjects(actorId).stream()
                .map(ProjectSummaryResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@AuthenticationPrincipal Long actorId, @RequestBody CreateProjectRequest request) {
        Project project = projectService.createProject(actorId, request.name(), request.description());
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), Role.TEAM_LEAD);
    }

    @GetMapping("/{projectId}")
    public ProjectResponse get(@AuthenticationPrincipal Long actorId, @PathVariable Long projectId) {
        Project project = projectService.getProject(actorId, projectId);
        Role role = project.getMembership(actorId).orElseThrow().getRole();
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), role);
    }
}
