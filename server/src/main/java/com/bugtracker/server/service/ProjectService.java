package com.bugtracker.server.service;

import com.bugtracker.server.dto.BugListFilter;
import com.bugtracker.server.dto.BugQuery;
import com.bugtracker.server.dto.ProjectResponse;
import com.bugtracker.server.dto.ProjectSummary;
import com.bugtracker.server.entity.*;
import com.bugtracker.server.entity.enums.Role;
import com.bugtracker.server.exception.NotFoundException;
import com.bugtracker.server.exception.ValidationException;
import com.bugtracker.server.repository.BugRepository;
import com.bugtracker.server.repository.ProjectRepository;
import com.bugtracker.server.repository.UserRepository;
import com.bugtracker.server.service.i.IProjectService;
import com.bugtracker.server.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final BugRepository bugRepository;
    private final AccessChecker accessChecker;
    private final FileStorage fileStorage;

    @Transactional
    public Project createProject(Long creatorId, String name, String description) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Введите название проекта", "name");
        }
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Project project = new Project(name, description);
        project.addMember(creator, Role.TEAM_LEAD);
        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectSummary> getProjects(Long userId) {
        return projectRepository.findByMember(userId).stream()
                .map(project -> new ProjectSummary(
                        project,
                        project.getMembership(userId).orElseThrow().getRole(),
                        bugRepository.countActive(project.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Project getProject(Long actorId, Long projectId) {
        accessChecker.requireMember(projectId, actorId);
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Проект не найден"));
    }

    @Transactional
    public Project updateProject(Long actorId, Long projectId, String name, String description) {
        accessChecker.requireRole(projectId, actorId, List.of(Role.TEAM_LEAD));
        if (name == null || name.isBlank()) {
            throw new ValidationException("Введите название проекта", "name");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Проект не найден"));
        project.update(name, description);
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long actorId, Long projectId) {
        accessChecker.requireRole(projectId, actorId, List.of(Role.TEAM_LEAD));
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Проект не найден"));
        BugQuery actual = new BugQuery(BugListFilter.ACTUAL, null, 1, Integer.MAX_VALUE);
        BugQuery closed = new BugQuery(BugListFilter.CLOSED, null, 1, Integer.MAX_VALUE);
        List<Bug> bugs = Stream.concat(
                        bugRepository.search(projectId, actorId, actual).stream(),
                        bugRepository.search(projectId, actorId, closed).stream())
                .toList();
        for (Bug bug : bugs) {
            for (Attachment attachment : bug.getAttachments()) {
                fileStorage.delete(attachment.getStorageKey());
            }
            for (Comment comment : bug.getComments()) {
                for (Attachment attachment : comment.getAttachments()) {
                    fileStorage.delete(attachment.getStorageKey());
                }
            }
        }
        projectRepository.delete(project);
    }
}