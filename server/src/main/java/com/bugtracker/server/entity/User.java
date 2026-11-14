package com.bugtracker.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name= "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(name= "password_hash", nullable = false)
    private String passwordHash;

    @Column(name= "is_deleted", nullable = false)
    private boolean isDeleted;

    protected User(){
    }

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }
    public Long getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getEmail(){
        return email;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public boolean isDeleted() {
        return isDeleted;
    }
}
