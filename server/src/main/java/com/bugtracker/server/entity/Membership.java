package com.bugtracker.server.entity;

import com.bugtracker.server.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Getter
@Table(name = "project_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership {

    @EmbeddedId
    private Key id = new Key();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("projectId")
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
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

    @Embeddable
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Key implements Serializable {

        @Column(name = "project_id")
        private Long projectId;

        @Column(name = "user_id")
        private Long userId;
    }
}
