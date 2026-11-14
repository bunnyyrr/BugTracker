package com.bugtracker.server.entity;

import com.bugtracker.server.entity.enums.Role;
import jakarta.persistence.*;

@Entity
@Table(name ="project_member")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected Membership() {
    }

    Membership(Project project, User user, Role role) {
        this.project = project;
        this.user = user;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public User getUser() {
        return user;
    }

    public Role getRole() {
        return role;
    }

    public boolean isTeamLead() {
        return role == Role.TEAM_LEAD;
    }
}
