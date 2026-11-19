package com.bugtracker.server.service;

import com.bugtracker.server.entity.Membership;
import com.bugtracker.server.entity.Project;
import com.bugtracker.server.exception.ForbiddenException;
import com.bugtracker.server.exception.NotFoundException;
import com.bugtracker.server.repository.ProjectRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AccessChecker {

    private final ProjectRepository projectRepository;

    public AccessChecker(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public Membership requireMember(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Проект не найден"));
        return project.getMembership(userId)
                .orElseThrow(() -> new ForbiddenException("Нет доступа к проекту"));
    }
}
