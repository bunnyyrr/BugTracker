package com.bugtracker.server.service.i;

import java.util.Optional;

public interface TokenProvider {
    String issue(Long userId);
    Optional<Long> validate(String token);
}
