package com.bugtracker.server.service.i;

public interface PasswordHasher {
    String hash(String password);
    boolean verify(String password, String hash);
}
