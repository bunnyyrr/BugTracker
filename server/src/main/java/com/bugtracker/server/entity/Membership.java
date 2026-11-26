package com.bugtracker.server.entity;

import com.bugtracker.server.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "project_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    Membership(Project project, User user, Role role) {
        this.project = project;
        this.user = user;
        this.role = role;
    }

    public boolean isTeamLead() {
        return role == Role.TEAM_LEAD;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}