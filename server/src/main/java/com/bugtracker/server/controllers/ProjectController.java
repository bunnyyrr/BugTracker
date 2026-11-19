package com.bugtracker.server.controllers;

import com.bugtracker.server.dto.CreateProjectRequest;
import com.bugtracker.server.dto.ProjectResponse;
import com.bugtracker.server.dto.ProjectSummary;
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
    public List<ProjectSummary> list(@AuthenticationPrincipal Long actorId) {
        return projectService.getProjects(actorId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@AuthenticationPrincipal Long actorId, @RequestBody CreateProjectRequest request) {
        return projectService.createProject(actorId, request.name(), request.description());
    }

    @GetMapping("/{projectId}")
    public ProjectResponse get(@AuthenticationPrincipal Long actorId, @PathVariable Long projectId) {
        return projectService.getProject(actorId, projectId);
    }
}
