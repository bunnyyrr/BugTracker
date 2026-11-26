package com.bugtracker.server.entity;

import com.bugtracker.server.entity.enums.Role;
import com.bugtracker.server.exception.ConflictException;
import com.bugtracker.server.exception.NotFoundException;
import com.bugtracker.server.exception.ValidationException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Table(name = "project")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Membership> memberships = new ArrayList<>();

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Optional<Membership> getMembership(Long userId) {
        return memberships.stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst();
    }

    public Membership addMember(User user, Role role) {
        if (role == null) {
            throw new ValidationException("Выберите роль");
        }
        if (getMembership(user.getId()).isPresent()) {
            throw new ConflictException("Пользователь уже участвует в проекте");
        }
        Membership membership = new Membership(this, user, role);
        memberships.add(membership);
        return membership;
    }

    public Membership changeMemberRole(Long userId, Role role) {
        Membership membership = getMembership(userId)
                .orElseThrow(() -> new NotFoundException("Участник не найден"));
        if (role != Role.TEAM_LEAD && isOnlyTeamLead(userId)) {
            throw new ConflictException("Нельзя оставить проект без тимлида");
        }
        membership.setRole(role);
        return membership;
    }

    public void removeMember(Long userId) {
        Membership membership = getMembership(userId)
                .orElseThrow(() -> new NotFoundException("Участник не найден"));
        if (isOnlyTeamLead(userId)) {
            throw new ConflictException("Нельзя оставить проект без тимлида");
        }
        memberships.remove(membership);
    }

    public int countTeamLeads() {
        return (int) memberships.stream().filter(Membership::isTeamLead).count();
    }

    public boolean isOnlyTeamLead(Long userId) {
        return countTeamLeads() == 1
                && getMembership(userId).map(Membership::isTeamLead).orElse(false);
    }

    public List<Membership> getMemberships() {
        return Collections.unmodifiableList(memberships);
    }
}