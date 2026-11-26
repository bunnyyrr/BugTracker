package com.bugtracker.server.service;

import com.bugtracker.server.entity.Bug;
import com.bugtracker.server.entity.Membership;
import com.bugtracker.server.entity.Project;
import com.bugtracker.server.entity.enums.Role;
import com.bugtracker.server.exception.ForbiddenException;
import com.bugtracker.server.exception.NotFoundException;
import com.bugtracker.server.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AccessChecker {

    private final ProjectRepository projectRepository;

    public Membership requireMember(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Проект не найден"));
        return project.getMembership(userId).orElseThrow(() -> new ForbiddenException("Вы не участник проекта"));
    }

    public Membership requireRole(Long projectId, Long userId, List<Role> allowed) {
        Membership membership = requireMember(projectId, userId);
        if (!allowed.contains(membership.getRole())) {
            throw new ForbiddenException("Недостаточно прав");
        }
        return membership;
    }

    public Membership requireReviewer(Bug bug, Long userId) {
        Membership membership = requireMember(bug.getProject().getId(), userId);
        boolean isTeamLead = membership.isTeamLead();
        boolean isAuthorTester = membership.getRole() == Role.TESTER && bug.getAuthor().getId().equals(userId);
        if (!isTeamLead && !isAuthorTester) {
            throw new ForbiddenException("Недостаточно прав");
        }
        return membership;
    }

    public Membership requireAuthorOrLead(Bug bug, Long userId) {
        return requireReviewer(bug, userId);
    }
}