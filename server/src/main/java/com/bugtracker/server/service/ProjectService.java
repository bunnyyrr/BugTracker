package com.bugtracker.server.service;

import com.bugtracker.server.dto.ProjectResponse;
import com.bugtracker.server.dto.ProjectSummary;
import com.bugtracker.server.entity.Membership;
import com.bugtracker.server.entity.Project;
import com.bugtracker.server.entity.User;
import com.bugtracker.server.entity.enums.Role;
import com.bugtracker.server.exception.NotFoundException;
import com.bugtracker.server.exception.ValidationException;
import com.bugtracker.server.repository.ProjectRepository;
import com.bugtracker.server.repository.UserRepository;
import com.bugtracker.server.service.i.IProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService implements IProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AccessChecker accessChecker;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          AccessChecker accessChecker) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.accessChecker = accessChecker;
    }

    @Override
    @Transactional
    public ProjectResponse createProject(Long creatorId, String name, String description) {
        String title = name == null ? "" : name.trim();
        if (title.isEmpty()) {
            throw new ValidationException("Название проекта обязательно", "name");
        }
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Project project = new Project(title, normalize(description));
        project.addMember(creator, Role.TEAM_LEAD);
        projectRepository.save(project);
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), Role.TEAM_LEAD);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectSummary> getProjects(Long userId) {
        return projectRepository.findByMember(userId).stream()
                .map(project -> new ProjectSummary(
                        project.getId(),
                        project.getName(),
                        project.getDescription(),
                        project.getMembership(userId).orElseThrow().getRole(),
                        projectRepository.countActiveBugs(project.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long actorId, Long projectId) {
        Membership membership = accessChecker.requireMember(projectId, actorId);
        Project project = membership.getProject();
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), membership.getRole());
    }

    private String normalize(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
