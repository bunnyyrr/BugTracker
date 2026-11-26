package com.bugtracker.server.service;

import com.bugtracker.server.dto.PublishPreview;
import com.bugtracker.server.dto.ReleaseSummary;
import com.bugtracker.server.entity.Bug;
import com.bugtracker.server.entity.Project;
import com.bugtracker.server.entity.Release;
import com.bugtracker.server.entity.User;
import com.bugtracker.server.entity.enums.Role;
import com.bugtracker.server.exception.ConflictException;
import com.bugtracker.server.exception.NotFoundException;
import com.bugtracker.server.exception.ValidationException;
import com.bugtracker.server.repository.BugRepository;
import com.bugtracker.server.repository.ProjectRepository;
import com.bugtracker.server.repository.ReleaseRepository;
import com.bugtracker.server.repository.UserRepository;
import com.bugtracker.server.service.i.IReleaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReleaseService implements IReleaseService {

    private final ReleaseRepository releaseRepository;
    private final ProjectRepository projectRepository;
    private final BugRepository bugRepository;
    private final UserRepository userRepository;
    private final AccessChecker accessChecker;

    @Override
    @Transactional(readOnly = true)
    public List<ReleaseSummary> getReleases(Long actorId, Long projectId) {
        accessChecker.requireMember(projectId, actorId);
        return releaseRepository.findByProject(projectId).stream()
                .map(release -> new ReleaseSummary(release, release.getBugCount(), release.getClosedBugCount()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Release getRelease(Long actorId, Long releaseId) {
        Release release = releaseRepository.findById(releaseId).orElseThrow(() -> new NotFoundException("Релиз не найден"));
        accessChecker.requireMember(release.getProject().getId(), actorId);
        return release;
    }

    @Override
    @Transactional
    public Release createRelease(Long actorId, Long projectId, String name, Instant plannedDate) {
        accessChecker.requireRole(projectId, actorId, List.of(Role.TEAM_LEAD));
        if (name == null || name.isBlank()) {
            throw new ValidationException("Введите название релиза", "name");
        }
        if (plannedDate == null) {
            throw new ValidationException("Укажите плановую дату", "plannedDate");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Проект не найден"));
        return releaseRepository.save(new Release(project, name, plannedDate));
    }

    @Override
    @Transactional
    public Release changeDate(Long actorId, Long releaseId, Instant newDate) {
        Release release = releaseRepository.findById(releaseId).orElseThrow(() -> new NotFoundException("Релиз не найден"));
        accessChecker.requireRole(release.getProject().getId(), actorId, List.of(Role.TEAM_LEAD));
        if (newDate == null) {
            throw new ValidationException("Укажите плановую дату", "plannedDate");
        }
        release.changeDate(newDate);
        return releaseRepository.save(release);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bug> getAvailableBugs(Long actorId, Long releaseId) {
        Release release = releaseRepository.findById(releaseId).orElseThrow(() -> new NotFoundException("Релиз не найден"));
        accessChecker.requireRole(release.getProject().getId(), actorId, List.of(Role.TEAM_LEAD));
        if (!release.isPlanned()) {
            throw new ConflictException("Релиз уже выпущен");
        }
        return bugRepository.findWithoutRelease(release.getProject().getId());
    }

    @Override
    @Transactional
    public Release addBugs(Long actorId, Long releaseId, List<Long> bugIds) {
        Release release = releaseRepository.findById(releaseId).orElseThrow(() -> new NotFoundException("Релиз не найден"));
        accessChecker.requireRole(release.getProject().getId(), actorId, List.of(Role.TEAM_LEAD));
        User actor = userRepository.findById(actorId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<Bug> bugs = bugRepository.findByIds(bugIds);
        if (bugs.size() != new HashSet<>(bugIds).size()) {
            throw new NotFoundException("Баг не найден");
        }
        for (Bug bug : bugs) {
            release.addBug(actor, bug);
        }
        return releaseRepository.save(release);
    }

    @Override
    @Transactional
    public Release removeBugs(Long actorId, Long releaseId, List<Long> bugIds) {
        Release release = releaseRepository.findById(releaseId).orElseThrow(() -> new NotFoundException("Релиз не найден"));
        accessChecker.requireRole(release.getProject().getId(), actorId, List.of(Role.TEAM_LEAD));
        User actor = userRepository.findById(actorId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<Bug> bugs = bugRepository.findByIds(bugIds);
        if (bugs.size() != new HashSet<>(bugIds).size()) {
            throw new NotFoundException("Баг не найден");
        }
        for (Bug bug : bugs) {
            release.removeBug(actor, bug);
        }
        return releaseRepository.save(release);
    }

    @Override
    @Transactional(readOnly = true)
    public PublishPreview getPublishPreview(Long actorId, Long releaseId) {
        Release release = releaseRepository.findById(releaseId).orElseThrow(() -> new NotFoundException("Релиз не найден"));
        accessChecker.requireRole(release.getProject().getId(), actorId, List.of(Role.TEAM_LEAD));
        Release next = releaseRepository.findNearestPlanned(release.getProject().getId(), releaseId).orElse(null);
        List<Bug> unclosedBugs = release.getBugs().stream()
                .filter(bug -> !bug.isClosed())
                .toList();
        return new PublishPreview(unclosedBugs, next);
    }

    @Override
    @Transactional
    public Release publish(Long actorId, Long releaseId) {
        Release release = releaseRepository.findById(releaseId).orElseThrow(() -> new NotFoundException("Релиз не найден"));
        accessChecker.requireRole(release.getProject().getId(), actorId, List.of(Role.TEAM_LEAD));
        User actor = userRepository.findById(actorId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Release next = releaseRepository.findNearestPlanned(release.getProject().getId(), releaseId).orElse(null);
        release.publish(actor, next);
        releaseRepository.save(release);
        if (next != null) {
            releaseRepository.save(next);
        }
        return release;
    }

    @Override
    @Transactional
    public void deleteRelease(Long actorId, Long releaseId) {
        Release release = releaseRepository.findById(releaseId).orElseThrow(() -> new NotFoundException("Релиз не найден"));
        accessChecker.requireRole(release.getProject().getId(), actorId, List.of(Role.TEAM_LEAD));
        if (!release.isPlanned()) {
            throw new ConflictException("Релиз уже выпущен");
        }
        User actor = userRepository.findById(actorId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        release.detachAllBugs(actor);
        releaseRepository.delete(release);
    }
}
