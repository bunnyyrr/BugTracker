package com.bugtracker.server.entity;

import com.bugtracker.server.entity.enums.HistoryEvent;
import com.bugtracker.server.entity.enums.ReleaseStatus;
import com.bugtracker.server.exception.ConflictException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "release")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Release {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "planned_date", nullable = false)
    private Instant plannedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReleaseStatus status;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "release")
    @OrderBy("id ASC")
    private List<Bug> bugs = new ArrayList<>();

    public Release(Project project, String name, Instant plannedDate) {
        this.project = project;
        this.name = name;
        this.plannedDate = plannedDate;
        this.status = ReleaseStatus.PLANNED;
    }

    public boolean isPlanned() {
        return status == ReleaseStatus.PLANNED;
    }

    public int getBugCount() {
        return bugs.size();
    }

    public int getClosedBugCount() {
        return (int) bugs.stream().filter(Bug::isClosed).count();
    }

    public void changeDate(Instant newDate) {
        requirePlanned();
        this.plannedDate = newDate;
    }

    public void addBug(User actor, Bug bug) {
        requirePlanned();
        if (!bug.getProject().getId().equals(project.getId())) {
            throw new ConflictException("Баг относится к другому проекту");
        }
        if (bug.isClosed()) {
            throw new ConflictException("Закрытый баг нельзя добавить в релиз");
        }
        if (bug.getRelease() != null) {
            throw new ConflictException("Баг уже входит в релиз");
        }
        changeBugRelease(actor, bug, this);
        bugs.add(bug);
    }

    public void removeBug(User actor, Bug bug) {
        requirePlanned();
        Release current = bug.getRelease();
        if (current == null || !current.getId().equals(id)) {
            throw new ConflictException("Баг не входит в этот релиз");
        }
        changeBugRelease(actor, bug, null);
        bugs.removeIf(b -> b.getId().equals(bug.getId()));
    }

    public void publish(User actor, Release nextRelease) {
        requirePlanned();
        this.status = ReleaseStatus.RELEASED;
        for (Bug bug : new ArrayList<>(bugs)) {
            if (bug.isClosed()) {
                continue;
            }
            changeBugRelease(actor, bug, nextRelease);
            bugs.removeIf(b -> b.getId().equals(bug.getId()));
            if (nextRelease != null) {
                nextRelease.bugs.add(bug);
            }
        }
    }

    public void detachAllBugs(User actor) {
        for (Bug bug : new ArrayList<>(bugs)) {
            changeBugRelease(actor, bug, null);
        }
        bugs.clear();
    }

    public List<Bug> getBugs() {
        return Collections.unmodifiableList(bugs);
    }

    private void changeBugRelease(User actor, Bug bug, Release target) {
        String oldValue = bug.getRelease() == null ? null : bug.getRelease().getName();
        String newValue = target == null ? null : target.getName();
        bug.setRelease(target);
        bug.recordHistory(actor, HistoryEvent.RELEASE_CHANGED, "release", oldValue, newValue);
    }

    private void requirePlanned() {
        if (!isPlanned()) {
            throw new ConflictException("Релиз уже выпущен");
        }
    }
}