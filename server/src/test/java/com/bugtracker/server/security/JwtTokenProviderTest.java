package com.bugtracker.server.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-1234";

    @Test
    void issuedTokenIsValid() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 24);

        String token = provider.issue(42L);

        assertThat(provider.validate(token)).contains(42L);
    }

    @Test
    void tokenSignedWithOtherSecretIsRejected() {
        String token = new JwtTokenProvider(SECRET, 24).issue(42L);
        JwtTokenProvider other = new JwtTokenProvider("another-secret-another-secret-another-1", 24);

        assertThat(other.validate(token)).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, -1);

        assertThat(provider.validate(provider.issue(42L))).isEmpty();
    }

    @Test
    void garbageIsRejected() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 24);

        assertThat(provider.validate("not-a-token")).isEmpty();
    }

    @Test
    void tooShortSecretFailsFast() {
        assertThatThrownBy(() -> new JwtTokenProvider("short", 24))
                .isInstanceOf(IllegalStateException.class);
    }
}
