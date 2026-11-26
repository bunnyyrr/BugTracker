package com.bugtracker.server.service;

import com.bugtracker.server.entity.Membership;
import com.bugtracker.server.entity.Project;
import com.bugtracker.server.entity.User;
import com.bugtracker.server.entity.enums.Role;
import com.bugtracker.server.exception.ConflictException;
import com.bugtracker.server.exception.NotFoundException;
import com.bugtracker.server.repository.ProjectRepository;
import com.bugtracker.server.repository.UserRepository;

import com.bugtracker.server.service.i.IBugService;
import com.bugtracker.server.service.i.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService implements IMemberService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AccessChecker accessChecker;
    private final IBugService bugService;

    @Override
    @Transactional(readOnly = true)
    public List<Membership> getMembers(Long actorId, Long projectId) {
        accessChecker.requireMember(projectId, actorId);
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Проект не найден"));
        return project.getMemberships();
    }

    @Override
    @Transactional(readOnly = true)
    public User findCandidate(Long actorId, Long projectId, String username) {
        accessChecker.requireRole(projectId, actorId, List.of(Role.TEAM_LEAD));
        User user = userRepository.findByUsername(username).filter(found -> !found.isDeleted()).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Проект не найден"));
        if (project.getMembership(user.getId()).isPresent()) {
            throw new ConflictException("Пользователь уже участвует в проекте");
        }
        return user;
    }

    @Override
    @Transactional
    public Membership addMember(Long actorId, Long projectId, String username, Role role) {
        accessChecker.requireRole(projectId, actorId, List.of(Role.TEAM_LEAD));
        User user = userRepository.findByUsername(username).filter(found -> !found.isDeleted()).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Проект не найден"));
        Membership membership = project.addMember(user, role);
        projectRepository.save(project);
        return membership;
    }

    @Override
    @Transactional
    public Membership changeRole(Long actorId, Long projectId, Long userId, Role role) {
        accessChecker.requireRole(projectId, actorId, List.of(Role.TEAM_LEAD));
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Проект не найден"));
        Membership membership = project.changeMemberRole(userId, role);
        if (role == Role.TESTER) {
            bugService.releaseAssignedBugs(actorId, projectId, userId);
        }
        projectRepository.save(project);
        return membership;
    }

    @Override
    @Transactional
    public void removeMember(Long actorId, Long projectId, Long userId) {
        accessChecker.requireRole(projectId, actorId, List.of(Role.TEAM_LEAD));
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Проект не найден"));
        project.removeMember(userId);
        bugService.releaseAssignedBugs(actorId, projectId, userId);
        projectRepository.save(project);
    }
}
