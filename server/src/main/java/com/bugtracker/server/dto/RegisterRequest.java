package com.bugtracker.server.dto;

public record RegisterRequest(String username, String email, String password) {
}
